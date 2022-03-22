package no.nav.dagpenger.innsyn

import com.auth0.jwk.JwkProvider
import com.auth0.jwt.interfaces.Claim
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.features.callIdMdc
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.document
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import mu.KotlinLogging
import no.nav.dagpenger.innsyn.Configuration.appName
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.modell.serde.SøknadJsonBuilder
import no.nav.dagpenger.innsyn.modell.serde.VedtakJsonBuilder
import no.nav.dagpenger.innsyn.tjenester.HenvendelseOppslag
import no.nav.dagpenger.innsyn.tjenester.ettersending.EttersendingSpleiser
import org.slf4j.event.Level
import java.time.LocalDate
import java.util.UUID

private val logger = KotlinLogging.logger { }
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal fun Application.innsynApi(
    jwkProvider: JwkProvider,
    issuer: String,
    clientId: String,
    personRepository: PersonRepository,
    henvendelseOppslag: HenvendelseOppslag,
    ettersendingSpleiser: EttersendingSpleiser
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
        exception<Throwable> { cause ->
            logger.error(cause) { "Feilet API kall. Feil: ${cause.message}" }
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
                    tom = tom,
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
                    fattetTom = fattetTom,
                )

                call.respond(vedtak.map { VedtakJsonBuilder(it).resultat() })
            }

            get("/ettersendelser") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val fnr = jwtPrincipal!!.fnr
                val ettersendelser = ettersendingSpleiser.hentEttersendelser(fnr)
                val httpKode = ettersendelser.determineHttpCode()
                if (ettersendelser.hasErrors()) {
                    val feilende = ettersendelser.failedSources()
                    val vellykkede = ettersendelser.successFullSources()
                    log.warn("Følgende kilder feilet: $feilende. Returnerer resultater fra $vellykkede sammen med HTTP-koden $httpKode.")
                }
                call.respond(httpKode, ettersendelser)
            }

            get("/paabegynte") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val fnr = jwtPrincipal!!.fnr
                val påbegynte = henvendelseOppslag.hentPåbegynte(fnr)
                call.respond(påbegynte)
            }
        }
    }
}

private fun String.asOptionalLocalDate() =
    takeIf(String::isNotEmpty)?.let { LocalDate.parse(it) }

private val JWTPrincipal.fnr: String
    get() = this.payload.claims.pid().asString()

private fun <V : Claim> Map<String, V>.pid() = firstNotNullOf { it.takeIf { it.key == "pid" } }.also {}.value
