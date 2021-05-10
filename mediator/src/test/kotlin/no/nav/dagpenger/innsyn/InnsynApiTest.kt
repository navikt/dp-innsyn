package no.nav.dagpenger.innsyn

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import no.nav.dagpenger.innsyn.helpers.InMemoryPersonRepository
import no.nav.dagpenger.innsyn.helpers.JwtStub
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.serde.SøknadListeJsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

internal class InnsynApiTest {
    private val testIssuer = "test-issuer"
    private val jwtStub = JwtStub(testIssuer)
    private val clientId = "id"

    @Test
    fun `test at bruker ikke har søknad`() {
        withTestApplication({
            innsynApi(
                InMemoryPersonRepository(),
                jwtStub.stubbedJwkProvider(),
                testIssuer,
                clientId
            )
        }) {
            autentisert("/soknader")
        }.apply {
            assertEquals(200, response.status()?.value)
        }
    }

    @Test
    fun `test at bruker har søknad`() {
        val personRepository = InMemoryPersonRepository().also {
            it.person("test@nav.no").also { person ->
                person.håndter(Søknad("1", "11", setOf(Oppgave.OppgaveType.søknadOppgave.ferdig("ferdig", ""))))
                it.lagre(person)
            }
        }
        withTestApplication({
            innsynApi(
                personRepository,
                jwtStub.stubbedJwkProvider(),
                testIssuer,
                clientId
            )
        }) {
            autentisert("/soknader")
        }.apply {
            assertEquals(200, response.status()?.value)
            assertTrue(response.content!!.contains("id"))
        }
    }

    @Test
    fun `test at vi kan finne en spesifikk søknad`() {
        var internId: UUID
        val personRepository = InMemoryPersonRepository().also {
            it.person("test@nav.no").also { person ->
                person.håndter(Søknad("1", "11", setOf(Oppgave.OppgaveType.søknadOppgave.ferdig("ferdig", ""))))
                it.lagre(person)
                internId = UUID.fromString(SøknadListeJsonBuilder(person).resultat().first()["id"].asText())
            }
        }
        withTestApplication({
            innsynApi(
                personRepository,
                jwtStub.stubbedJwkProvider(),
                testIssuer,
                clientId
            )
        }) {
            autentisert("/soknader/$internId")
        }.apply {
            assertEquals(200, response.status()?.value)
            assertTrue(response.content!!.contains(internId.toString()))
        }
    }

    private fun TestApplicationEngine.autentisert(
        endepunkt: String,
        token: String = jwtStub.createTokenFor("test@nav.no", "id"),
        httpMethod: HttpMethod = HttpMethod.Get,
        body: String? = null
    ) = handleRequest(httpMethod, endepunkt) {
        addHeader(
            HttpHeaders.ContentType,
            ContentType.Application.Json.toString()
        )
        addHeader(HttpHeaders.Authorization, "Bearer $token")
        body?.also { setBody(it) }
    }
}
