package no.nav.dagpenger.innsyn

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.request.document
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import mu.KotlinLogging
import no.nav.dagpenger.innsyn.db.PersonRepository
import org.slf4j.event.Level

private val logger = KotlinLogging.logger { }
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal fun Application.innsynApi(
    personRepository: PersonRepository
    /*jwkProvider: JwkProvider,
    issuer: String,
    clientId: String*/
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
    /*install(Authentication) {
        jwt {
            verifier(jwkProvider, issuer)
            realm = appName
            validate { credentials ->
                try {
                    requireNotNull(credentials.payload.audience) {
                        "Auth: Missing audience in token"
                    }
                    require(credentials.payload.audience.contains(clientId)) {
                        "Auth: Valid audience not found in claims"
                    }
                    JWTPrincipal(credentials.payload)
                } catch (e: Throwable) {
                    logger.error(e) { "JWT validerte ikke" }
                    null
                }
            }
        }
    }*/

    routing {
        get("/søknad/{fnr}") {
            val fnr = call.parameters["fnr"].toString()
            val person = personRepository.person(fnr)

            val harSøknadUnderBehandling = person.finnUferdigeOppgaverAv(Dagpenger.vedtak).isNotEmpty()

            sikkerlogg.info { "Hentet person $fnr. Person har søknad: ($harSøknadUnderBehandling). Person: $person" }
            call.respond("OK")
        }
    }
}
