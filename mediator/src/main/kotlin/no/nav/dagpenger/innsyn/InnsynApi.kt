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
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
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
import org.slf4j.event.Level
import java.time.LocalDate

private val logger = KotlinLogging.logger { }
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal fun Application.innsynApi(
    jwkProvider: JwkProvider,
    issuer: String,
    clientId: String,
    personRepository: PersonRepository
) {
    install(CallLogging) {
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
                requireNotNull(credentials.payload.claims["pid"]) {
                    "Token må inneholde fødselsnummer for personen"
                }

                JWTPrincipal(credentials.payload)
            }
        }
    }

    routing {
        authenticate {
            get("/soknader") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val fnr = jwtPrincipal!!.fnr
                val fom = call.request.queryParameters["fom"]?.asOptionalLocalDate()
                val tom = call.request.queryParameters["tom"]?.asOptionalLocalDate()
                val type = call.request.queryParameters.getAll("type")?.map { SøknadsType.valueOf(it) } ?: emptyList()
                val søknader = personRepository.hentSøknaderFor(
                    fnr,
                    fom = fom,
                    tom = tom,
                    // TODO: få til dette
                    // type = type
                )
                val map = søknader.map { SøknadJsonBuilder(it).resultat() }

                call.respond(map)
            }
            get("/soknader/{id}") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val fnr = jwtPrincipal!!.fnr
                val person = personRepository.person(fnr)
                // call.respondText { SøknadsprosessJsonBuilder(person, UUID.fromString(call.parameters["id"])).resultat().toString() }
            }
        }
    }
}

private fun String.asOptionalLocalDate() =
    takeIf(String::isNotEmpty)?.let { LocalDate.parse(it) }

private val JWTPrincipal.fnr get() = this.payload!!.claims["pid"]!!.asString()
