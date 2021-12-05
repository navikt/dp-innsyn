package no.nav.dagpenger.innsyn

import com.auth0.jwk.JwkProvider
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
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
import io.ktor.features.callIdMdc
import io.ktor.jackson.jackson
import io.ktor.request.document
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import mu.KotlinLogging
import no.nav.dagpenger.innsyn.Configuration.appName
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad.SøknadsType
import no.nav.dagpenger.innsyn.modell.serde.SøknadJsonBuilder
import no.nav.dagpenger.innsyn.modell.serde.VedtakJsonBuilder
import org.slf4j.event.Level
import java.time.LocalDate
import java.util.UUID

private val logger = KotlinLogging.logger { }
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal fun Application.innsynApi(
    jwkProvider: JwkProvider,
    issuer: String,
    clientId: String,
    personRepository: PersonRepository
) {
    install(CallId) {
        header("Nav-Call-Id")
        generate { UUID.randomUUID().toString() }
        verify { callId: String -> callId.isNotEmpty() }
    }
    install(CallLogging) {
        callIdMdc("x_callId")
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
                logger.info { "Validerer token med claims: ${credentials.payload.claims.keys}" }
                requireNotNull(credentials.payload.claims["pid"]) {
                    "Token må inneholde fødselsnummer for personen"
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
                val fom = call.request.queryParameters["søktFom"]?.asOptionalLocalDate()
                val tom = call.request.queryParameters["søktTom"]?.asOptionalLocalDate()
                val fom2 = call.request.queryParameters["soktFom"]?.asOptionalLocalDate()
                val tom2 = call.request.queryParameters["soktTom"]?.asOptionalLocalDate()
                val søknader = personRepository.hentSøknaderFor(
                    fnr,
                    fom = if (fom !== null) fom else fom2,
                    tom = if (tom !== null) tom else tom2,
                )

                call.respond(søknader.map { SøknadJsonBuilder(it).resultat() })
            }
            get("/vedtak") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val fnr = jwtPrincipal!!.fnr
                val fattetFom = call.request.queryParameters["fattetFom"]?.asOptionalLocalDate()
                val fattetTom = call.request.queryParameters["fattetTom"]?.asOptionalLocalDate()
                val type = call.request.queryParameters.getAll("type")?.map { SøknadsType.valueOf(it) } ?: emptyList()
                val vedtak = personRepository.hentVedtakFor(
                    fnr,
                    fattetFom = fattetFom,
                    fattetTom = fattetTom,
                )

                call.respond(vedtak.map { VedtakJsonBuilder(it).resultat() })
            }
        }
    }
}

private fun String.asOptionalLocalDate() =
    takeIf(String::isNotEmpty)?.let { LocalDate.parse(it) }

private val JWTPrincipal.fnr get() = this.payload!!.claims["pid"]!!.asString()
