package no.nav.dagpenger.innsyn

import io.ktor.http.HttpHeaders
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.server.testing.withTestApplication
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class InnsynApiTest {
    private val rapid = TestRapid()
    private val mediator = Mediator(rapid)

    @BeforeEach
    fun reset() {
        rapid.reset()
    }

    @Test
    fun `tar imot status-behov og pusher på websocket`() {
        @Language("JSON")
        val request =
            """{
          "behov": "Søknader"
        }
            """.trimIndent()

        @Language("JSON")
        val behov =
            """{
          "@behov": ["Søknader"],
          "session": "5A0FEE87-50FA-48B4-B0A5-A973DBB1AAD3",
          "fødselsnummer": "123123123"
        }
            """.trimIndent()

        @Language("JSON")
        fun løstBehov(sessionId: String) =
            """{
          "@behov": ["Søknader"],
          "fødselsnummer": "123123123",
          "session": "$sessionId",
          "@løsning": {
            "Søknader": [
              {
                "journalpostId": 1,
                "fagsakId": 2
              }
            ]
          }
        }
            """.trimIndent()

        withTestApplication({ innsynApi(mediator) }) {
            handleWebSocketConversation(
                "/ws",
                {
                    addHeader(HttpHeaders.Authorization, "Basic MTIzMTIzMTIzOnBhc3NvcmQxCg==")
                }
            ) { incoming, outgoing ->
                val sessionId = (incoming.receive() as Frame.Text).readText()
                outgoing.send(Frame.Text(request))
                // rapid.sendTestMessage(behov)
                rapid.sendTestMessage(løstBehov(sessionId))
                val incoming = (incoming.receive() as Frame.Text).readText()
                assertTrue(incoming.contains("@løsning"))
            }
        }
    }
}
