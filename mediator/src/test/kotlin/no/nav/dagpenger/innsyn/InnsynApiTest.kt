package no.nav.dagpenger.innsyn

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import no.nav.dagpenger.innsyn.db.PostgresPersonRepository
import no.nav.dagpenger.innsyn.helpers.JwtStub
import no.nav.dagpenger.innsyn.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Sakstilknytning
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad.SøknadsType.NySøknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class InnsynApiTest {
    private val testIssuer = "test-issuer"
    private val jwtStub = JwtStub(testIssuer)
    private val clientId = "id"

    @Test
    fun `test at bruker ikke har søknad`() = withMigratedDb {
        withTestApplication({
            innsynApi(
                jwtStub.stubbedJwkProvider(),
                testIssuer,
                clientId,
                PostgresPersonRepository()
            )
        }) {
            autentisert("/soknader")
        }.apply {
            assertEquals(HttpStatusCode.NotFound, response.status())
        }
    }

    @Test
    fun `test at bruker har søknad`() = withMigratedDb {
        val personRepository = PostgresPersonRepository().also {
            it.person("test@nav.no").also { person ->
                person.håndter(
                    Søknad(
                        "1",
                        "1",
                        "NAV01",
                        NySøknad,
                        Kanal.Digital,
                        LocalDateTime.now()
                    )
                )
                person.håndter(
                    Søknad(
                        "2",
                        "11",
                        "NAV01",
                        NySøknad,
                        Kanal.Digital,
                        LocalDateTime.now()
                    )
                )
                person.håndter(
                    Søknad(
                        "3",
                        "12",
                        "NAV01",
                        NySøknad,
                        Kanal.Digital,
                        LocalDateTime.now()
                    )
                )
                person.håndter(
                    Søknad(
                        "4",
                        "13",
                        "NAV01",
                        NySøknad,
                        Kanal.Digital,
                        LocalDateTime.now()
                    )
                )
                person.håndter(
                    Søknad(
                        "5",
                        "14",
                        "NAV01",
                        NySøknad,
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
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        null
                    )
                )
                it.lagre(person)
            }
        }
        withTestApplication({
            innsynApi(
                jwtStub.stubbedJwkProvider(),
                testIssuer,
                clientId,
                personRepository,
            )
        }) {
            val dagensDato = LocalDate.now()
            autentisert("/soknader?fom=$dagensDato&tom=$dagensDato&type=Gjenopptak&type=NySøknad")
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            assertTrue(response.content!!.contains(NySøknad.toString()))
            println(response.content)
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
            HttpHeaders.Accept,
            ContentType.Application.Json.toString()
        )
        addHeader(
            HttpHeaders.ContentType,
            ContentType.Application.Json.toString()
        )
        addHeader(HttpHeaders.Authorization, "Bearer $token")
        body?.also { setBody(it) }
    }
}
