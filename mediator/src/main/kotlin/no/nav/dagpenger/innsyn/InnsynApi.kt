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
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.document
import io.ktor.server.request.header
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import mu.KotlinLogging
import no.nav.dagpenger.innsyn.Configuration.APP_NAME
import no.nav.dagpenger.innsyn.api.models.BehandlingsstatusResponse
import no.nav.dagpenger.innsyn.behandlingsstatus.AvgjørBehandlingsstatus
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.mapper.PåbegyntSøknadMapper
import no.nav.dagpenger.innsyn.mapper.SøknadMapper
import no.nav.dagpenger.innsyn.mapper.VedtakMapper
import no.nav.dagpenger.innsyn.tjenester.PåbegyntOppslag
import org.slf4j.event.Level
import java.time.LocalDate
import java.util.UUID

private val logger = KotlinLogging.logger { }

internal fun Application.innsynApi(
    jwkProvider: JwkProvider,
    issuer: String,
    clientId: String,
    personRepository: PersonRepository,
    påbegyntOppslag: PåbegyntOppslag,
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
                "metrics",
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
                },
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
            realm = APP_NAME
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
        swaggerUI(path = "openapi", swaggerFile = "innsyn-api.yaml")

        authenticate {
            get("/soknad") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val fnr = jwtPrincipal!!.fnr
                val fom = call.request.queryParameters["soktFom"]?.asOptionalLocalDate()
                val tom = call.request.queryParameters["soktTom"]?.asOptionalLocalDate()
                val søknader =
                    personRepository.hentSøknaderFor(
                        fnr,
                        fom = fom,
                        tom = tom,
                    )

                call.respond(søknader.map { SøknadMapper(it).response })
            }
            get("/vedtak") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val fnr = jwtPrincipal!!.fnr
                val fattetFom = call.request.queryParameters["fattetFom"]?.asOptionalLocalDate()
                val fattetTom = call.request.queryParameters["fattetTom"]?.asOptionalLocalDate()
                val vedtak =
                    personRepository.hentVedtakFor(
                        fnr,
                        fattetFom = fattetFom,
                        fattetTom = fattetTom,
                    )

                call.respond(vedtak.map { VedtakMapper(it).response })
            }

            get("/behandlingsstatus") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val fnr = jwtPrincipal!!.fnr
                val fom =
                    call.request.queryParameters["fom"]
                        ?: throw IllegalArgumentException("Mangler fom queryparameter i url")
                val behandlingsstatus = avgjørBehandlingsstatus.hentStatus(fnr, LocalDate.parse(fom))
                val status =
                    behandlingsstatus?.let {
                        BehandlingsstatusResponse.Behandlingsstatus.valueOf(it.name)
                    } ?: BehandlingsstatusResponse.Behandlingsstatus.Ukjent
                call.respond(HttpStatusCode.OK, BehandlingsstatusResponse(status))
            }

            get("/paabegynte") {
                val requestId: String? =
                    call.request.header("Nav-Consumer-Id") ?: call.request.header(HttpHeaders.XRequestId)
                val token = call.request.jwt()
                val påbegyntSøknadFraNySøknadsdialog =
                    try {
                        påbegyntOppslag.hentPåbegyntSøknad(token, requestId)?.let {
                            listOf(
                                PåbegyntSøknadMapper(dto = it, erNySøknadsdialog = true).response,
                            )
                        } ?: emptyList()
                    } catch (e: Exception) {
                        logger.error(e) { "Klarte ikke å hente påbegynt søknad fra dp-soknad " }
                        emptyList()
                    }
                call.respond(påbegyntSøknadFraNySøknadsdialog)
            }
        }
    }
}

private fun String.asOptionalLocalDate(): LocalDate? = takeIf(String::isNotEmpty)?.let { LocalDate.parse(it) }

private val JWTPrincipal.fnr: String
    get() = this.payload.claims.pid().asString()

internal fun ApplicationRequest.jwt(): String =
    this.parseAuthorizationHeader().let { authHeader ->
        (authHeader as? HttpAuthHeader.Single)?.blob ?: throw IllegalArgumentException("JWT not found")
    }

private fun <V : Claim> Map<String, V>.pid() = firstNotNullOf { it.takeIf { it.key == "pid" } }.value
