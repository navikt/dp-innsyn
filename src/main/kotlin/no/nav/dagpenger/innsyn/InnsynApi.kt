package no.nav.dagpenger.innsyn

import io.ktor.application.Application
import io.ktor.application.install
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

private val logger = KotlinLogging.logger { }
private val sikkerLogg = KotlinLogging.logger("tjenestekall")

internal fun Application.innsynApi(
    statusMediator: StatusMediator
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

    install(WebSockets)

    routing {
        val wsConnections = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketSession>())
        // authenticate {
        webSocket("/ws") {
            try {
                wsConnections += WebSocketSession(this).also {
                    val fnr = "123" // call.authentication.principal
                    statusMediator.ønskerStatus(fnr, it)
                }.also {
                    it.listen()
                }
            } catch (e: Error) {
                println(e)
            } finally {
                wsConnections -= this
            }
        }
        // }
    }
}

private class WebSocketSession(
    private val session: DefaultWebSocketSession,
) : StatusObserver, DefaultWebSocketSession by session {
    suspend fun listen() {
        while (true) {
            val frame = incoming.receive()
            logger.info { frame.frameType }

            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    println("incoming")
                    println(text)
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
