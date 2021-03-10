package no.nav.dagpenger.innsyn

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.basic
import io.ktor.features.CallLogging
import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.request.document
import io.ktor.routing.routing
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import mu.KotlinLogging
import org.slf4j.event.Level
import java.util.Collections
import java.util.UUID

private val logger = KotlinLogging.logger { }
private val sikkerLogg = KotlinLogging.logger("tjenestekall")

internal fun Application.innsynApi(
    mediator: Mediator
    /*jwkProvider: JwkProvider,
    issuer: String,
    clientId: String*/
) {
    /*internal fun Application.søknadApi(
        subscribe: (MeldingObserver) -> Unit,
        publish: (String) -> Unit
    ) {
    */

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
    install(Authentication) {
        basic {
            realm = "ktor"
            validate { credentials ->
                UserIdPrincipal(credentials.name)
            }
        }
    }

    install(WebSockets)

    routing {
        val wsConnections = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketSession>())
        authenticate {
            webSocket("/ws") {
                try {
                    val fnr = call.authentication.principal.toString()
                    val sessionId = UUID.randomUUID()
                    wsConnections += WebSocketSession(this, sessionId, fnr, mediator).also {
                        it.send(Frame.Text(sessionId.toString()))
                    }.also {
                        it.listen()
                    }
                } catch (e: Error) {
                    println(e)
                } finally {
                    wsConnections -= this
                }
            }
        }
    }
}

private class WebSocketSession(
    private val session: DefaultWebSocketSession,
    override val uuid: UUID,
    private val fødselsnummer: String,
    private val mediator: Mediator
) : StatusObserver, DefaultWebSocketSession by session {
    suspend fun listen() {
        while (true) {
            val frame = incoming.receive()
            logger.info { frame.frameType }

            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    mediator.ønskerStatus(fødselsnummer, this)
                }
                is Frame.Binary -> TODO()
                is Frame.Close -> TODO()
                is Frame.Ping -> TODO()
                is Frame.Pong -> TODO()
            }
        }
    }

    override suspend fun statusOppdatert(status: String) {
        send(Frame.Text(status))
    }
}
