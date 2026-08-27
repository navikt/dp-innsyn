package no.nav.dagpenger.innsyn

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.interfaces.Claim
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson3.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.document
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import no.nav.dagpenger.innsyn.Configuration.APP_NAME
import no.nav.dagpenger.innsyn.aktivdagpenger.AktivDagpengerTjeneste
import no.nav.dagpenger.innsyn.aktivdagpenger.AlltidInaktivDagpengerTjeneste
import no.nav.dagpenger.innsyn.api.models.AktivDagpengerettResponse
import no.nav.dagpenger.innsyn.api.models.BehandlingsstatusResponse
import no.nav.dagpenger.innsyn.behandlingsstatus.AvgjørBehandlingsstatus
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.mapper.SøknadMapper.toResponse
import no.nav.dagpenger.innsyn.mapper.VedtakMapper.toResponse
import org.slf4j.event.Level
import tools.jackson.core.util.DefaultIndenter
import tools.jackson.core.util.DefaultPrettyPrinter
import tools.jackson.databind.SerializationFeature
import java.time.LocalDate
import java.util.UUID

private val logger = KotlinLogging.logger { }

internal fun Application.innsynApi(
    jwkProvider: JwkProvider,
    issuer: String,
    clientId: String,
    personRepository: PersonRepository,
    aktivDagpengerTjeneste: AktivDagpengerTjeneste = AlltidInaktivDagpengerTjeneste,
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
            defaultPrettyPrinter(
                DefaultPrettyPrinter().apply {
                    indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance())
                    indentObjectsWith(DefaultIndenter("  ", "\n"))
                },
            )
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

                call.respond(søknader.map { it.toResponse() })
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

                call.respond(vedtak.map { it.toResponse() })
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

            get("/aktiv-dagpenger") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val personIdent = jwtPrincipal!!.fnr
                val harAktivDagpengerett = aktivDagpengerTjeneste.harAktivDagpengerett(personIdent)
                call.respond(HttpStatusCode.OK, AktivDagpengerettResponse(harAktivDagpengerett = harAktivDagpengerett))
            }
        }
    }
}

private fun String.asOptionalLocalDate(): LocalDate? = takeIf(String::isNotEmpty)?.let { LocalDate.parse(it) }

private val JWTPrincipal.fnr: String
    get() =
        this.payload.claims
            .pid()
            .asString()

private fun <V : Claim> Map<String, V>.pid() = firstNotNullOf { it.takeIf { it.key == "pid" } }.value
