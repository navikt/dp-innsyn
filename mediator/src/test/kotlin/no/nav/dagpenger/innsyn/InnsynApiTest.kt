package no.nav.dagpenger.innsyn

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.FerdigBehandlet
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
    private val testIssuer = "test-issuer"
    private val jwtStub = JwtStub(testIssuer)
    private val clientId = "id"
    private val henvendelseOppslag = mockk<HenvendelseOppslag>()
    private val ettersendingSpleiser = mockk<EttersendingSpleiser>()

    @Test
    fun `test at bruker ikke har søknad`() = withMigratedDb {
        withTestApplication({
            innsynApi(
                jwtStub.stubbedJwkProvider(),
                testIssuer,
                clientId,
                PostgresPersonRepository(),
                henvendelseOppslag,
                ettersendingSpleiser
            )
        }) {
            autentisert("/soknad")
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("[ ]", response.content!!)
        }
    }

    @Test
    fun `test at bruker har søknad`() = withMigratedDb {
        val personRepository = PostgresPersonRepository().also {
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
        withTestApplication({
            innsynApi(
                jwtStub.stubbedJwkProvider(),
                testIssuer,
                clientId,
                personRepository,
                henvendelseOppslag,
                ettersendingSpleiser
            )
        }) {
            val fom = LocalDate.now().minusDays(100)
            val dagensDato = LocalDate.now()
            autentisert("/soknad?soktFom=$fom&soktTom=$dagensDato")
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            assertTrue(response.content!!.contains(NySøknad.toString()))
            assertTrue(response.content!!.contains(Innsending.Vedlegg.Status.LastetOpp.toString()))
            assertTrue(response.content!!.contains("Søknad om"))
        }
    }

    @Test
    fun `test at bruker har søknad som ikke kommer med`() = withMigratedDb {
        val personRepository = PostgresPersonRepository().also {
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
        withTestApplication({
            innsynApi(
                jwtStub.stubbedJwkProvider(),
                testIssuer,
                clientId,
                personRepository,
                henvendelseOppslag,
                ettersendingSpleiser
            )
        }) {
            val dagensDato = LocalDate.now()
            autentisert("/soknad?soktFom=${dagensDato.minusDays(30)}&soktTom=$dagensDato")
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            assertFalse(response.content!!.contains(NySøknad.toString()))
        }
    }

    @Test
    fun `test at bruker har vedtak`() = withMigratedDb {
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
                henvendelseOppslag,
                ettersendingSpleiser
            )
        }) {
            val dagensDato = LocalDate.now()
            autentisert("/vedtak?fattetFom=$dagensDato&fattetTom=$dagensDato")
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            assertTrue(response.content!!.contains(Vedtak.Status.INNVILGET.toString()))
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
        withTestApplication({
            innsynApi(
                jwtStub.stubbedJwkProvider(),
                testIssuer,
                clientId,
                personRepository,
                henvendelseOppslag,
                ettersendingSpleiser
            )
        }) {
            val dagensDato = LocalDate.now()
            autentisert(
                "/vedtak?fattetFom=${dagensDato.minusDays(30)}&fattetTom=$dagensDato",
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            assertFalse(response.content!!.contains(Vedtak.Status.INNVILGET.toString()))
        }
    }

    @Test
    fun `test at bruker kan hente ut ettersendelser`() {
        val ettersendingSpleiser = mockk<EttersendingSpleiser>()
        coEvery { ettersendingSpleiser.hentEttersendelser(any()) } returns MultiSourceResultObjectMother.giveMeSuccessfulResult()
        withTestApplication({
            innsynApi(
                jwtStub.stubbedJwkProvider(),
                testIssuer,
                clientId,
                mockk<PostgresPersonRepository>(),
                henvendelseOppslag,
                ettersendingSpleiser
            )
        }) {
            autentisert("/ettersendelser")
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
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
        withTestApplication({
            innsynApi(
                jwtStub.stubbedJwkProvider(),
                testIssuer,
                clientId,
                mockk<PostgresPersonRepository>(),
                henvendelseOppslag,
                ettersendingSpleiser
            )
        }) {
            autentisert("/ettersendelser")
        }.apply {
            with(jacksonObjectMapper().readTree(response.content)) {
                assertTrue(this.has("failedSources"))
                assertTrue(this.has("results"))
            }
            assertEquals(HttpStatusCode.OK, response.status())
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
        withTestApplication({
            innsynApi(
                jwtStub.stubbedJwkProvider(),
                testIssuer,
                clientId,
                mockk<PostgresPersonRepository>(),
                henvendelseOppslag,
                ettersendingSpleiser
            )
        }) {
            autentisert("/paabegynte")
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `får behandlingsstatus FerdigBehandlet når det er 1 søknad og 1 vedtak`() = withMigratedDb {
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
                henvendelseOppslag,
                ettersendingSpleiser
            )
        }) {
            val dagensDato = LocalDate.now()
            autentisert("/behandlingsstatus?fom=$dagensDato")
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            assertTrue(response.content!!.contains(FerdigBehandlet.toString()))
        }
    }

    @Test
    fun `InternalServerError når man ikke kan parse fom dato`() = withMigratedDb {
        val mocketApi: Application.() -> Unit = {
            innsynApi(
                jwtStub.stubbedJwkProvider(),
                testIssuer,
                clientId,
                PostgresPersonRepository(),
                henvendelseOppslag,
                ettersendingSpleiser
            )
        }

        withTestApplication(mocketApi) {
            autentisert("/behandlingsstatus?fom=ugyldig_fom")
        }.apply {
            assertEquals(HttpStatusCode.InternalServerError, response.status())
        }

        withTestApplication(mocketApi) {
            autentisert("/behandlingsstatus")
        }.apply {
            assertEquals(HttpStatusCode.InternalServerError, response.status())
        }
    }

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
        addHeader("Nav-Call-Id", "random call id")
        addHeader("Nav-Consumer-Id", "dp-test")
        body?.also { setBody(it) }
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
