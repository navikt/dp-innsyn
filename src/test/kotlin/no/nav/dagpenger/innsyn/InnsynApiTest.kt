package no.nav.dagpenger.innsyn

import io.ktor.http.HttpHeaders
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.server.testing.withTestApplication
import no.nav.dagpenger.innsyn.meldinger.ØnskerStatusMelding
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class InnsynApiTest {
    private val rapid = TestRapid()
    private val mediator = StatusMediator(rapid)
    private val dings = StatusDings(rapid)

    @BeforeEach
    fun reset() {
        rapid.reset()
    }

    @Test
    fun `tar imot status-behov og pusher på websocket`() {
        withTestApplication({ innsynApi(mediator) }) {
            handleWebSocketConversation(
                "/ws",
                {
                    addHeader(HttpHeaders.Authorization, "Basic QWxhZGRpbjpPcGVuU2VzYW1l")
                }
            ) { incoming, outgoing ->
                outgoing.send(Frame.Text("123"))
                rapid.sendTestMessage(ØnskerStatusMelding("123").toJson())

                val incoming = (incoming.receive() as Frame.Text).readText()
                println(incoming)
                Assertions.assertTrue(incoming.contains("123123"))
            }
        }
    }
}
