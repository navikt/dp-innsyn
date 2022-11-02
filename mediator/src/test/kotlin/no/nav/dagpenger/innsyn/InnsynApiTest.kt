package no.nav.dagpenger.innsyn

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
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
import no.nav.dagpenger.innsyn.tjenester.PåbegyntOppslag
import no.nav.dagpenger.innsyn.tjenester.PåbegyntSøknadDto
import no.nav.dagpenger.innsyn.tjenester.ettersending.EttersendingSpleiser
import no.nav.dagpenger.innsyn.tjenester.paabegynt.Påbegynt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

internal class InnsynApiTest {
    private val testIssuer = "test-issuer"
    private val jwtStub = JwtStub(testIssuer)
    private val clientId = "id"
    private val henvendelseOppslag = mockk<HenvendelseOppslag>()
    private val ettersendingSpleiser = mockk<EttersendingSpleiser>()
    private val jacksonObjectMapper = jacksonObjectMapper()

    @Test
    fun `test at bruker ikke har søknad`() = withMigratedDb {
        testApplication {
            application {
                innsynApi(
                    jwtStub.stubbedJwkProvider(),
                    testIssuer,
                    clientId,
                    PostgresPersonRepository(),
                    henvendelseOppslag,
                    ettersendingSpleiser,
                    mockk()
                )
            }
            client.autentisert("/soknad").let { response ->
                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals("[ ]", response.bodyAsText())
            }
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
        testApplication {
            application {
                innsynApi(
                    jwtStub.stubbedJwkProvider(),
                    testIssuer,
                    clientId,
                    personRepository,
                    henvendelseOppslag,
                    ettersendingSpleiser,
                    mockk()
                )
            }
            val fom = LocalDate.now().minusDays(100)
            val dagensDato = LocalDate.now()
            client.autentisert("/soknad?soktFom=$fom&soktTom=$dagensDato").let { response ->
                assertEquals(HttpStatusCode.OK, response.status)
                response.bodyAsText().let { content ->
                    assertTrue(content.contains(NySøknad.toString()))
                    assertTrue(content.contains(Innsending.Vedlegg.Status.LastetOpp.toString()))
                    assertTrue(content.contains("Søknad om"))
                }
            }
        }
    }

    @Test
    fun `Søknad fra ny søknadsdialog får legacy false og motsatt`() = withMigratedDb {
        val søknadIdGammeltFormat = "1"
        val søknadIdNyttFormat = UUID.randomUUID().toString()
        val personRepository = PostgresPersonRepository().also {
            it.person("test@nav.no").also { person ->
                person.håndter(
                    søknad(søknadIdGammeltFormat, "1", LocalDateTime.now().minusDays(90))
                )
                person.håndter(
                    søknad(søknadIdNyttFormat, "11", LocalDateTime.now().minusDays(90))
                )
                it.lagre(person)
            }
        }
        testApplication {
            application {
                innsynApi(
                    jwtStub.stubbedJwkProvider(),
                    testIssuer,
                    clientId,
                    personRepository,
                    henvendelseOppslag,
                    ettersendingSpleiser,
                    mockk()
                )
            }
            val fom = LocalDate.now().minusDays(100)
            val dagensDato = LocalDate.now()
            client.autentisert("/soknad?soktFom=$fom&soktTom=$dagensDato").let { response ->
                ObjectMapper().readTree(response.bodyAsText()).let { jsonNode ->
                    assertTrue(jsonNode[0]["erNySøknadsdialog"].asBoolean()) { "Forventet at erNySøknadsdialog: true. $jsonNode" }
                    assertFalse(jsonNode[1]["erNySøknadsdialog"].asBoolean()) { "Forventet at erNySøknadsdialog: false.  $jsonNode" }
                }
            }
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
        testApplication {
            application {
                innsynApi(
                    jwtStub.stubbedJwkProvider(),
                    testIssuer,
                    clientId,
                    personRepository,
                    henvendelseOppslag,
                    ettersendingSpleiser,
                    mockk()
                )
            }
            val dagensDato = LocalDate.now()
            client.autentisert("/soknad?soktFom=${dagensDato.minusDays(30)}&soktTom=$dagensDato").let { response ->
                assertEquals(HttpStatusCode.OK, response.status)
                assertFalse(response.bodyAsText().contains(NySøknad.toString()))
            }
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
        testApplication {
            application {
                innsynApi(
                    jwtStub.stubbedJwkProvider(),
                    testIssuer,
                    clientId,
                    personRepository,
                    henvendelseOppslag,
                    ettersendingSpleiser,
                    mockk()
                )
            }
            val dagensDato = LocalDate.now()
            client.autentisert("/vedtak?fattetFom=$dagensDato&fattetTom=$dagensDato").let { response ->
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
            application {
                innsynApi(
                    jwtStub.stubbedJwkProvider(),
                    testIssuer,
                    clientId,
                    personRepository,
                    henvendelseOppslag,
                    ettersendingSpleiser,
                    mockk()
                )
            }
            val dagensDato = LocalDate.now()
            client.autentisert("/vedtak?fattetFom=${dagensDato.minusDays(30)}&fattetTom=$dagensDato").let { response ->
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
            application {
                innsynApi(
                    jwtStub.stubbedJwkProvider(),
                    testIssuer,
                    clientId,
                    mockk<PostgresPersonRepository>(),
                    henvendelseOppslag,
                    ettersendingSpleiser,
                    mockk()
                )
            }
            client.autentisert("/ettersendelser").let { response ->
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
            application {
                innsynApi(
                    jwtStub.stubbedJwkProvider(),
                    testIssuer,
                    clientId,
                    mockk<PostgresPersonRepository>(),
                    henvendelseOppslag,
                    ettersendingSpleiser,
                    mockk(),
                )
            }
            client.autentisert("/ettersendelser").let { response ->
                assertEquals(HttpStatusCode.OK, response.status)
                jacksonObjectMapper.readTree(response.bodyAsText()).let {
                    assertTrue(it.has("failedSources"))
                    assertTrue(it.has("results"))
                }
            }
        }
    }

    @Test
    fun `test at bruker kan hente ut påbegynte søknader fra gammel og ny stack`() {
        val påbegyntOppslagMock = mockk<PåbegyntOppslag>()
        val henvendelseOppslag = mockk<HenvendelseOppslag>()
        val søknadId = "bid"
        val nå = ZonedDateTime.now()
        val påbegynte = listOf(
            Påbegynt(
                tittel = "En tittel oversatt fra kodeverk",
                behandlingsId = søknadId,
                søknadId = søknadId,
                sistEndret = nå,
                erNySøknadsdialog = false
            )
        )
        val uuid = UUID.randomUUID()
        val påbegyntNySøknadsdialog = PåbegyntSøknadDto(
            uuid = uuid,
            opprettet = ZonedDateTime.of(LocalDateTime.MAX, ZoneId.of("Europe/Oslo")),
            sistEndret = nå
        )
        coEvery { henvendelseOppslag.hentPåbegynte(any()) } returns påbegynte
        coEvery { påbegyntOppslagMock.hentPåbegyntSøknad(any()) } returns påbegyntNySøknadsdialog

        testApplication {
            application {
                innsynApi(
                    jwtStub.stubbedJwkProvider(),
                    testIssuer,
                    clientId,
                    mockk<PostgresPersonRepository>(),
                    henvendelseOppslag,
                    ettersendingSpleiser,
                    påbegyntOppslagMock
                )
            }
            val response = client.autentisert("/paabegynte")
            assertEquals(HttpStatusCode.OK, response.status)
            val json = response.bodyAsText().let { jacksonObjectMapper.readTree(it) }

            val fraGammelSøknadsdialog = json[0]
            assertEquals("En tittel oversatt fra kodeverk", fraGammelSøknadsdialog["tittel"].asText())
            assertEquals("En tittel oversatt fra kodeverk", fraGammelSøknadsdialog["tittel"].asText())
            assertEquals("bid", fraGammelSøknadsdialog["søknadId"].asText())
            assertEquals("bid", fraGammelSøknadsdialog["behandlingsId"].asText())
            assertFalse(fraGammelSøknadsdialog["erNySøknadsdialog"].asBoolean())
            assertEquals(nå.toOffsetDateTime(), fraGammelSøknadsdialog["sistEndret"].asText().let { ZonedDateTime.parse(it).toOffsetDateTime() })

            val fraNySøknadsdialog = json[1]
            assertEquals("Søknad om dagpenger", fraNySøknadsdialog["tittel"].asText())
            assertEquals(uuid.toString(), fraNySøknadsdialog["søknadId"].asText())
            assertEquals(uuid.toString(), fraNySøknadsdialog["behandlingsId"].asText())
            assertTrue(fraNySøknadsdialog["erNySøknadsdialog"].asBoolean())
            assertEquals(nå.toOffsetDateTime(), fraNySøknadsdialog["sistEndret"].asText().let { ZonedDateTime.parse(it).toOffsetDateTime() })
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
        testApplication {
            application {
                innsynApi(
                    jwtStub.stubbedJwkProvider(),
                    testIssuer,
                    clientId,
                    personRepository,
                    henvendelseOppslag,
                    ettersendingSpleiser,
                    mockk()
                )
            }
            val dagensDato = LocalDate.now()
            client.autentisert("/behandlingsstatus?fom=$dagensDato").let { response ->
                assertEquals(HttpStatusCode.OK, response.status)
                assertTrue(response.bodyAsText().contains(FerdigBehandlet.toString()))
            }
        }
    }

    @Test
    fun `InternalServerError når man ikke kan parse fom dato`() = withMigratedDb {
        testApplication {
            application {
                innsynApi(
                    jwtStub.stubbedJwkProvider(),
                    testIssuer,
                    clientId,
                    PostgresPersonRepository(),
                    henvendelseOppslag,
                    ettersendingSpleiser,
                    mockk()
                )
            }
            assertEquals(
                HttpStatusCode.InternalServerError,
                client.autentisert("/behandlingsstatus?fom=ugyldig_fom").status
            )
            assertEquals(
                HttpStatusCode.InternalServerError,
                client.autentisert("/behandlingsstatus").status
            )
        }
    }

    private suspend fun HttpClient.autentisert(
        endepunkt: String,
        token: String = jwtStub.createTokenFor("test@nav.no", "id"),
        httpMethod: HttpMethod = HttpMethod.Get,
        body: String? = null
    ): HttpResponse {
        return this.request {
            this.url(endepunkt)
            this.method = httpMethod
            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                append(HttpHeaders.Authorization, "Bearer $token")
                append("Nav-Call-Id", "random call id")
                append("Nav-Consumer-Id", "dp-test")
            }
            body?.let { this.setBody(it) }
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
