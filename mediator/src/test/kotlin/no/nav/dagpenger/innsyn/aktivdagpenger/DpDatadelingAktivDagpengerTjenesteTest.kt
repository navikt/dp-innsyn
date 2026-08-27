package no.nav.dagpenger.innsyn.aktivdagpenger

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.jackson3.jackson
import io.mockk.mockk
import no.nav.dagpenger.oauth2.CachedOauth2Client
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

internal class DpDatadelingAktivDagpengerTjenesteTest {
    private val iDag = Instant.parse("2026-08-17T10:00:00Z")
    private val clock = Clock.fixed(iDag, ZoneId.of("UTC"))

    @Test
    fun `skal returnere true når åpen periode overlapper i dag`() {
        val tjeneste =
            tjenesteMedRespons(
                """
                {
                  "personIdent": "12345678901",
                  "perioder": [
                    { "fraOgMedDato": "2026-08-10", "tilOgMedDato": null, "kilde": "x", "ytelseType": "DAGPENGER" }
                  ]
                }
                """.trimIndent(),
                HttpStatusCode.OK,
            )

        val harAktivRett = kotlinx.coroutines.runBlocking { tjeneste.harAktivDagpengerett("12345678901") }

        assertTrue(harAktivRett)
    }

    @Test
    fun `skal returnere false når periode er avsluttet før i dag`() {
        val tjeneste =
            tjenesteMedRespons(
                """
                {
                  "personIdent": "12345678901",
                  "perioder": [
                    { "fraOgMedDato": "2026-08-01", "tilOgMedDato": "2026-08-16", "kilde": "x", "ytelseType": "DAGPENGER" }
                  ]
                }
                """.trimIndent(),
                HttpStatusCode.OK,
            )

        val harAktivRett = kotlinx.coroutines.runBlocking { tjeneste.harAktivDagpengerett("12345678901") }

        assertFalse(harAktivRett)
    }

    @Test
    fun `skal returnere false når dp-datadeling feiler`() {
        val tjeneste = tjenesteMedRespons("""{"feil":"boom"}""", HttpStatusCode.InternalServerError)

        val harAktivRett = kotlinx.coroutines.runBlocking { tjeneste.harAktivDagpengerett("12345678901") }

        assertFalse(harAktivRett)
    }

    private fun tjenesteMedRespons(
        body: String,
        status: HttpStatusCode,
    ): DpDatadelingAktivDagpengerTjeneste {
        val engine =
            MockEngine { request ->
                assertTrue(request.url.toString().contains("/dagpenger/datadeling/v1/perioder"))
                respond(
                    content = body,
                    status = status,
                    headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                )
            }

        val client =
            HttpClient(engine) {
                install(ContentNegotiation) {
                    jackson()
                }
            }

        return DpDatadelingAktivDagpengerTjeneste(
            httpClient = client,
            oauth2Client = mockk<CachedOauth2Client>(relaxed = true),
            perioderUrl = "https://dp-datadeling.ekstern.dev.nav.no/dagpenger/datadeling/v1/perioder",
            scope = "api://dev-gcp.teamdagpenger.dp-datadeling/.default",
            clock = clock,
            tokenUtsteder = { "utstedt-token" },
        )
    }
}
