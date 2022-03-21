package no.nav.dagpenger.innsyn.tjenester

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.innsyn.objectmother.ExternalEttersendingObjectMother
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.ZonedDateTime

internal class HenvendelseOppslagTest {

    private val objectMapper = jacksonObjectMapper()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .registerModule(JavaTimeModule())
        .writer(DefaultPrettyPrinter())

    @Test
    fun `Skal klare å utlede riktig returtype ut i fra generics definisjonen for ettersendelser`() {
        val expectedReturnValues = ExternalEttersendingObjectMother.giveMeEttersendelserForDAGOgBIL()
        val mockHttpClient = mockHttpClientWithReturnValue(expectedReturnValues)
        val henvendelseOppslag = henvendelseOppslagWithMockClient(mockHttpClient, "test1")

        assertDoesNotThrow { runBlocking { henvendelseOppslag.hentEttersendelser("123") } }
    }

    @Test
    fun `Skal klare å utlede riktig returtype ut i fra generics definisjonen for påbegynte`() {
        val expectedReturnValues = listOf(ExternalPåbegynt("bid", "kode", ZonedDateTime.now()))
        val mockHttpClient = mockHttpClientWithReturnValue(expectedReturnValues)
        val henvendelseOppslag = henvendelseOppslagWithMockClient(mockHttpClient, "test2")
        assertDoesNotThrow { runBlocking { henvendelseOppslag.hentPåbegynte("123") } }
    }

    private fun mockHttpClientWithReturnValue(returnObject: Any) = MockEngine {
        val jsonResponse = objectMapper.writeValueAsString(returnObject)
        respond(
            content = jsonResponse,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }

    private fun henvendelseOppslagWithMockClient(mockHttpClient: MockEngine, baseName: String) =
        HenvendelseOppslag(
            dpProxyUrl = "dummyUrl",
            tokenProvider = { "dummyToken" },
            httpClientEngine = mockHttpClient,
            baseName = baseName
        )
}
