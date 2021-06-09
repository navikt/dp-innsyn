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
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Sakstilknytning
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class InnsynApiTest {
    private val testIssuer = "test-issuer"
    private val jwtStub = JwtStub(testIssuer)
    private val clientId = "id"

    @Disabled
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

    @Disabled
    @Test
    fun `test at bruker har søknad`() {
        val personRepository = InMemoryPersonRepository().also {
            it.person("test@nav.no").also { person ->
                person.håndter(
                    Søknad(
                        "1",
                        "1",
                        "NAV01",
                        Søknad.SøknadsType.NySøknad,
                        Kanal.Digital,
                        LocalDateTime.now()
                    )
                )
                person.håndter(
                    Søknad(
                        "2",
                        "11",
                        "NAV01",
                        Søknad.SøknadsType.NySøknad,
                        Kanal.Digital,
                        LocalDateTime.now()
                    )
                )
                person.håndter(
                    Søknad(
                        "3",
                        "12",
                        "NAV01",
                        Søknad.SøknadsType.NySøknad,
                        Kanal.Digital,
                        LocalDateTime.now()
                    )
                )
                person.håndter(
                    Søknad(
                        "4",
                        "13",
                        "NAV01",
                        Søknad.SøknadsType.NySøknad,
                        Kanal.Digital,
                        LocalDateTime.now()
                    )
                )
                person.håndter(
                    Søknad(
                        "5",
                        "14",
                        "NAV01",
                        Søknad.SøknadsType.NySøknad,
                        Kanal.Digital,
                        LocalDateTime.now()
                    )
                )
                person.håndter(Sakstilknytning("11", "arenaId"))
                person.håndter(
                    Vedtak(
                        "2",
                        "arenaId",
                        Vedtak.Status.INNVILGET,
                    )
                )
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
            assertTrue(response.content!!.contains("søknadstidspunkt"))
            assertTrue(response.content!!.contains("vedtakstidspunkt"))
            assertTrue(response.content!!.contains("LØPENDE"))
        }
    }

    /*@Test
    fun `test at vi kan finne en spesifikk søknad`() {
        var internId: UUID
        val personRepository = InMemoryPersonRepository().also {
            it.person("test@nav.no").also { person ->
                person.håndter(Søknad("1", "11", "NAV01", Søknad.SøknadsType.NySøknad, Kanal.Digital))
                it.lagre(person)
                //internId = UUID.fromString(SøknadsprosessJsonBuilder(person).resultat().first()["id"].asText())
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
    }*/

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
