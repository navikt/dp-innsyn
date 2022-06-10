package no.nav.dagpenger.innsyn

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.dagpenger.innsyn.common.KildeType
import no.nav.dagpenger.innsyn.db.PostgresPersonRepository
import no.nav.dagpenger.innsyn.helpers.JwtStub
import no.nav.dagpenger.innsyn.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Sakstilknytning
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad.SøknadsType.NySøknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.objectmother.MultiSourceResultObjectMother
import no.nav.dagpenger.innsyn.tjenester.HenvendelseOppslag
import no.nav.dagpenger.innsyn.tjenester.ettersending.EttersendingSpleiser
import no.nav.dagpenger.innsyn.tjenester.paabegynt.Påbegynt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

internal class InnsynApiTest {
    private val jwtStub = JwtStub("test-issuer")

    private fun Application.innsynApi(
        repository: PostgresPersonRepository = PostgresPersonRepository(),
        henvendelseOppslag: HenvendelseOppslag = mockk(),
        ettersendingSpleiser: EttersendingSpleiser = mockk()
    ) {
        innsynApi(
            jwtStub.stubbedJwkProvider(),
            "test-issuer",
            "id",
            repository,
            henvendelseOppslag,
            ettersendingSpleiser
        )
    }

    @Test
    fun `test at bruker ikke har søknad`() = withMigratedDb {
        testApplication {
            application { innsynApi() }

            autentisert("/soknad").also { response ->
                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals("[ ]", response.bodyAsText())
            }
        }
    }

    @Test
    fun `test at bruker har søknad`() = withMigratedDb {
        val repository = PostgresPersonRepository().also {
            it.person("test@nav.no").also { person ->
                person.håndter(
                    søknad("1", "1", LocalDateTime.now().minusDays(90), "Søknad om")
                )
                person.håndter(
                    søknad("2", "11", LocalDateTime.now().minusDays(90))
                )
                person.håndter(
                    søknad("3", "12", LocalDateTime.now().minusDays(90))
                )
                person.håndter(
                    søknad("4", "13", LocalDateTime.now().minusDays(90))
                )
                person.håndter(
                    søknad("5", "14", LocalDateTime.now().minusDays(90))
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
        testApplication {
            application { innsynApi(repository = repository) }

            val fom = LocalDate.now().minusDays(100)
            val dagensDato = LocalDate.now()

            autentisert("/soknad?soktFom=$fom&soktTom=$dagensDato").also { response ->
                assertEquals(HttpStatusCode.OK, response.status)
                with(response.bodyAsText()) {
                    assertTrue(this.contains(NySøknad.toString()))
                    assertTrue(this.contains(Innsending.Vedlegg.Status.LastetOpp.toString()))
                    assertTrue(this.contains("Søknad om"))
                }
            }
        }
    }

    @Test
    fun `test at bruker har søknad som ikke kommer med`() = withMigratedDb {
        val repository = PostgresPersonRepository().also {
            it.person("test@nav.no").also { person ->
                person.håndter(
                    søknad("1", "1", LocalDateTime.now().minusDays(90))
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
        testApplication {
            application { innsynApi(repository = repository) }

            val dagensDato = LocalDate.now()
            autentisert("/soknad?soktFom=${dagensDato.minusDays(30)}&soktTom=$dagensDato").also { response ->
                assertEquals(HttpStatusCode.OK, response.status)
                assertFalse(response.bodyAsText().contains(NySøknad.toString()))
            }
        }
    }

    @Test
    fun `test at bruker har vedtak`() = withMigratedDb {
        PostgresPersonRepository().also {
            it.person("test@nav.no").also { person ->
                person.håndter(
                    søknad()
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
        testApplication {
            application { innsynApi() }

            val dagensDato = LocalDate.now()
            autentisert("/vedtak?fattetFom=$dagensDato&fattetTom=$dagensDato").also { response ->
                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains(Vedtak.Status.INNVILGET.toString()))
            }
        }
    }

    @Test
    fun `test at bruker har vedtak som ikke er innefor tiden`() = withMigratedDb {
        val personRepository = PostgresPersonRepository().also {
            it.person("test@nav.no").also { person ->
                person.håndter(
                    søknad()
                )
                person.håndter(Sakstilknytning("11", "arenaId"))
                person.håndter(
                    Vedtak(
                        "2",
                        "arenaId",
                        Vedtak.Status.INNVILGET,
                        LocalDateTime.now().minusDays(90),
                        LocalDateTime.now(),
                        null
                    )
                )
                it.lagre(person)
            }
        }
        testApplication {
            application { innsynApi(repository = personRepository) }

            val dagensDato = LocalDate.now()
            autentisert("/vedtak?fattetFom=${dagensDato.minusDays(30)}&fattetTom=$dagensDato").also { response ->
                assertEquals(HttpStatusCode.OK, response.status)
                assertFalse(response.bodyAsText().contains(Vedtak.Status.INNVILGET.toString()))
            }
        }
    }

    @Test
    fun `test at bruker kan hente ut ettersendelser`() {
        val ettersendingSpleiser = mockk<EttersendingSpleiser>()
        coEvery { ettersendingSpleiser.hentEttersendelser(any()) } returns MultiSourceResultObjectMother.giveMeSuccessfulResult()
        testApplication {
            application { innsynApi(ettersendingSpleiser = ettersendingSpleiser) }

            autentisert("/ettersendelser").also { response ->
                assertEquals(HttpStatusCode.OK, response.status)
            }
        }
    }

    @Test
    fun `test at bruker kan hente ut ettersendelser når én kilde feiler`() {
        val ettersendingSpleiser = mockk<EttersendingSpleiser>()
        val enFeiletOgEnVellykket =
            MultiSourceResultObjectMother.giveMeSuccessfulResult(KildeType.DB) + MultiSourceResultObjectMother.giveMeFailedResult(
                KildeType.HENVENDELSE
            )

        coEvery { ettersendingSpleiser.hentEttersendelser(any()) } returns enFeiletOgEnVellykket
        testApplication {
            application { innsynApi(ettersendingSpleiser = ettersendingSpleiser) }

            autentisert("/ettersendelser").also { response ->
                assertEquals(HttpStatusCode.OK, response.status)
                with(jacksonObjectMapper().readTree(response.bodyAsText())) {
                    assertTrue(this.has("failedSources"))
                    assertTrue(this.has("results"))
                }
            }
        }
    }

    @Test
    fun `test at bruker kan hente ut påbegynte søknader`() {
        val henvendelseOppslag = mockk<HenvendelseOppslag>()
        val påbegynte = listOf(
            Påbegynt(
                "En tittel oversatt fra kodeverk",
                "bid",
                ZonedDateTime.now()
            )
        )
        coEvery { henvendelseOppslag.hentPåbegynte(any()) } returns påbegynte
        testApplication {
            application { innsynApi(henvendelseOppslag = henvendelseOppslag) }

            autentisert("/paabegynte").also { response ->
                assertEquals(HttpStatusCode.OK, response.status)
            }
        }
    }

    private suspend fun ApplicationTestBuilder.autentisert(
        endepunkt: String,
        token: String = jwtStub.createTokenFor("test@nav.no", "id"),
        httpMethod: HttpMethod = HttpMethod.Get,
        body: String? = null,
    ): HttpResponse {
        val client = createClient {
            install(ContentNegotiation) {
                jackson { }
            }
        }
        return client.request(endepunkt) {
            method = httpMethod
            headers {
                header(HttpHeaders.Authorization, "Bearer $token")
                header("Nav-Call-Id", "random call id")
                header("Nav-Consumer-Id", "dp-test")
            }
            body?.let { setBody(it) }
        }
    }

    private fun søknad(
        søknadId: String = "1",
        journalpostId: String = "1",
        datoInnsendt: LocalDateTime = LocalDateTime.now(),
        tittel: String? = null
    ) = Søknad(
        søknadId = søknadId,
        journalpostId = journalpostId,
        skjemaKode = "NAV 04-01.03",
        søknadsType = NySøknad,
        kanal = Kanal.Digital,
        datoInnsendt = datoInnsendt,
        vedlegg = listOf(Innsending.Vedlegg("123", "navn", Innsending.Vedlegg.Status.LastetOpp)),
        tittel = tittel
    )
}
