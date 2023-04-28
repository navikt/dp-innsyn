package no.nav.dagpenger.innsyn.tjenester

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class PåbegynteOppslagTest {

    private val testTokenProvider: (token: String, audience: String) -> String = { _, _ -> "testToken" }
    private val baseUrl = "http://baseurl"
    private val soknadAudience = "dp-soknad"
    private val subjectToken = "subjectToken"

    @Test
    fun `Henter påbegynt søknad`() {
        val søknadUuid = UUID.randomUUID()
        val opprettet = ZonedDateTime.of(LocalDateTime.MAX, ZoneId.of("Europe/Oslo"))
        val sistEndret = ZonedDateTime.of(LocalDateTime.MAX, ZoneId.of("Europe/Oslo"))
        runBlocking {
            val påbegyntOppslag = PåbegyntOppslag(
                baseUrl,
                soknadAudience,
                testTokenProvider,
                engine = MockEngine { request ->
                    assertEquals("$baseUrl/arbeid/dagpenger/soknadapi/soknad/paabegynt", request.url.toString())
                    assertEquals(HttpMethod.Get, request.method)
                    assertEquals(
                        "Bearer ${testTokenProvider.invoke(subjectToken, soknadAudience)}",
                        request.headers[HttpHeaders.Authorization],
                    )
                    val jsonResponse = objectMapper.writeValueAsString(
                        PåbegyntSøknadDto(
                            uuid = søknadUuid,
                            opprettet = opprettet,
                            sistEndret = sistEndret,
                        ),
                    )
                    respond(
                        content = jsonResponse,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            )

            val påbegyntResponse = påbegyntOppslag.hentPåbegyntSøknad(subjectToken = "testToken")
            assertEquals(søknadUuid, påbegyntResponse!!.uuid)
            assertEquals(opprettet.toOffsetDateTime(), påbegyntResponse.opprettet.toOffsetDateTime())
            assertEquals(sistEndret.toOffsetDateTime(), påbegyntResponse.sistEndret.toOffsetDateTime())
        }
    }

    private val objectMapper = jacksonObjectMapper()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .registerModule(JavaTimeModule())
}
