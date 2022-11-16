package no.nav.dagpenger.innsyn

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.interfaces.Claim
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.document
import io.ktor.server.request.header
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import mu.KotlinLogging
import no.nav.dagpenger.innsyn.Configuration.appName
import no.nav.dagpenger.innsyn.behandlingsstatus.AvgjørBehandlingsstatus
import no.nav.dagpenger.innsyn.behandlingsstatus.BehandlingsstatusDTO
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.modell.serde.VedtakJsonBuilder
import no.nav.dagpenger.innsyn.tjenester.HenvendelseOppslag
import no.nav.dagpenger.innsyn.tjenester.PåbegyntOppslag
import no.nav.dagpenger.innsyn.tjenester.ettersending.EttersendingSpleiser
import no.nav.dagpenger.innsyn.tjenester.paabegynt.Påbegynt
import org.slf4j.event.Level
import java.time.LocalDate
import java.util.UUID

private val logger = KotlinLogging.logger { }

internal fun Application.innsynApi(
    jwkProvider: JwkProvider,
    issuer: String,
    clientId: String,
    personRepository: PersonRepository,
    henvendelseOppslag: HenvendelseOppslag,
    ettersendingSpleiser: EttersendingSpleiser,
    påbegyntOppslag: PåbegyntOppslag
) {
    install(CallId) {
        header("Nav-Call-Id")
        generate { UUID.randomUUID().toString() }
        verify { callId: String -> callId.isNotEmpty() }
    }
    install(CallLogging) {
        callIdMdc("x_callId")
        disableDefaultColors()
        mdc("x_consumerId") { it.request.headers["Nav-Consumer-Id"] }

        level = Level.DEBUG
        filter { call ->
            !setOf(
                "isalive",
                "isready",
                "metrics"
            ).contains(call.request.document())
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Kall mot ${call.request.path()} feilet. Feilmelding: ${cause.message}" }
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    install(DefaultHeaders)
    install(Compression)
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            setDefaultPrettyPrinter(
                DefaultPrettyPrinter().apply {
                    indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                    indentObjectsWith(DefaultIndenter("  ", "\n"))
                }
            )
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            registerModule(JavaTimeModule())
        }
    }

    install(Authentication) {
        jwt {
            verifier(jwkProvider, issuer) {
                withAudience(clientId)
            }
            realm = appName
            validate { credentials ->
                requireNotNull(credentials.payload.claims.pid()) {
                    "Token må inneholde fødselsnummer for personen i enten pid claim"
                }

                JWTPrincipal(credentials.payload)
            }
        }
    }
    val avgjørBehandlingsstatus = AvgjørBehandlingsstatus(personRepository)
    routing {
        authenticate {
            get("/soknad") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val fnr = jwtPrincipal!!.fnr
                val fom = call.request.queryParameters["soktFom"]?.asOptionalLocalDate()
                val tom = call.request.queryParameters["soktTom"]?.asOptionalLocalDate()
                val søknader = personRepository.hentSøknaderFor(
                    fnr,
                    fom = fom,
                    tom = tom
                )

                call.respond(søknader.map { SøknadJsonBuilder(it).resultat() })
            }
            get("/vedtak") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val fnr = jwtPrincipal!!.fnr
                val fattetFom = call.request.queryParameters["fattetFom"]?.asOptionalLocalDate()
                val fattetTom = call.request.queryParameters["fattetTom"]?.asOptionalLocalDate()
                val vedtak = personRepository.hentVedtakFor(
                    fnr,
                    fattetFom = fattetFom,
                    fattetTom = fattetTom
                )

                call.respond(vedtak.map { VedtakJsonBuilder(it).resultat() })
            }

            get("/behandlingsstatus") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val fnr = jwtPrincipal!!.fnr
                val fom = call.request.queryParameters["fom"]
                    ?: throw IllegalArgumentException("Mangler fom queryparameter i url")
                val behandlingsstatus = avgjørBehandlingsstatus.hentStatus(fnr, LocalDate.parse(fom))

                call.respond(HttpStatusCode.OK, BehandlingsstatusDTO(behandlingsstatus))
            }

            get("/ettersendelser") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val fnr = jwtPrincipal!!.fnr
                val ettersendelser = ettersendingSpleiser.hentEttersendelser(fnr)
                val httpKode = ettersendelser.determineHttpCode()
                if (ettersendelser.hasErrors()) {
                    val feilende = ettersendelser.failedSources()
                    val vellykkede = ettersendelser.successFullSources()
                    logger.warn("Følgende kilder feilet: $feilende. Returnerer resultater fra $vellykkede sammen med HTTP-koden $httpKode.")
                }
                call.respond(httpKode, ettersendelser)
            }

            get("/paabegynte") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val requestId: String? = call.request.header("Nav-Consumer-Id") ?: call.request.header(HttpHeaders.XRequestId)
                val fnr = jwtPrincipal!!.fnr
                val token = call.request.jwt()
                val påbegynte = async { henvendelseOppslag.hentPåbegynte(fnr) }
                val påbegyntSøknadFraNySøknadsdialog = async {
                    try {
                        påbegyntOppslag.hentPåbegyntSøknad(token, requestId)?.let {
                            listOf(
                                Påbegynt(
                                    søknadId = it.uuid.toString(),
                                    behandlingsId = it.uuid.toString(),
                                    sistEndret = it.sistEndret,
                                    tittel = "Søknad om dagpenger",
                                    erNySøknadsdialog = true
                                )
                            )
                        } ?: emptyList()
                    } catch (e: Exception) {
                        logger.error(e) { "Klarte ikke å hente påbegynt søknad fra dp-soknad " }
                        emptyList()
                    }
                }
                val søknader = awaitAll(påbegynte, påbegyntSøknadFraNySøknadsdialog).flatten()
                call.respond(søknader)
            }
        }
    }
}

private fun String.asOptionalLocalDate(): LocalDate? =
    takeIf(String::isNotEmpty)?.let { LocalDate.parse(it) }

private val JWTPrincipal.fnr: String
    get() = this.payload.claims.pid().asString()

internal fun ApplicationRequest.jwt(): String = this.parseAuthorizationHeader().let { authHeader ->
    (authHeader as? HttpAuthHeader.Single)?.blob ?: throw IllegalArgumentException("JWT not found")
}

private fun <V : Claim> Map<String, V>.pid() = firstNotNullOf { it.takeIf { it.key == "pid" } }.value
