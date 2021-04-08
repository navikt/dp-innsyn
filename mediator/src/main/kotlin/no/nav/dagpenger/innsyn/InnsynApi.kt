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
    }

    routing {
        authenticate {
            get("/søknad") {
                val fnr = call.authentication.principal<JWTPrincipal>()
                val person = personRepository.person(fnr!!.payload!!.subject)
                val harSendtSøknad = person.harFerdigeOppgaverAv(Dagpenger.søknadOppgave)
                val harManglendeVedlegg = person.harUferdigeOppgaverAv(Dagpenger.vedleggOppgave)
                val harSøknadUnderBehandling = person.harUferdigeOppgaverAv(Dagpenger.vedleggOppgave)

                sikkerlogg.info { "Personen har søkt: $harSendtSøknad, manglende vedlegg: $harManglendeVedlegg, og søknad under behandling: $harSøknadUnderBehandling" }
                call.respondText { PersonJsonBuilder(person).resultat().toString() }
            }
        }
    }
}
