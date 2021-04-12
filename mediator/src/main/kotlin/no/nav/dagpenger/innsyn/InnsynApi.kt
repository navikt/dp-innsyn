package no.nav.dagpenger.innsyn

import com.auth0.jwk.JwkProvider
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallLogging
import io.ktor.request.document
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import mu.KotlinLogging
import no.nav.dagpenger.innsyn.Configuration.appName
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.modell.serde.PersonJsonBuilder
import org.slf4j.event.Level

private val logger = KotlinLogging.logger { }
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal fun Application.innsynApi(
    personRepository: PersonRepository,
    jwkProvider: JwkProvider,
    issuer: String,
    clientId: String
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
            get("/soknad") {
                val jwtPrincipal = call.authentication.principal<JWTPrincipal>()
                val fnr = jwtPrincipal!!.fnr
                logger.info { "Fikk request." }
                sikkerlogg.info { "Fikk request. Fnr: $fnr. Subject: ${jwtPrincipal!!.payload!!.subject}. JWT ser slik ut: ${jwtPrincipal!!.payload}" }
                val person = personRepository.person(fnr)

                call.respondText { PersonJsonBuilder(person).resultat().toString() }
            }
        }
    }
}

private val JWTPrincipal.fnr get() = this.payload!!.claims["pid"]!!.asString()
