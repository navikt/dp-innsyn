package no.nav.dagpenger.innsyn

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
import no.nav.dagpenger.innsyn.api.models.SoknadResponse
import no.nav.dagpenger.innsyn.api.models.VedtakResponse
import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.FerdigBehandlet
import no.nav.dagpenger.innsyn.db.PostgresPersonRepository
import no.nav.dagpenger.innsyn.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.innsyn.modell.DataRequest
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Sakstilknytning
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad.SøknadsType.NySøknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.tjenester.PåbegyntOppslag
import no.nav.dagpenger.innsyn.tjenester.PåbegyntSøknadDto
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

internal class InnsynApiTest {
    private val objectMapper =
        jacksonObjectMapper()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .registerModule(JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    companion object {
        private const val TOKEN_X_ISSUER_ID = "token-x-issuer"
        private const val AZURE_ISSUER_ID = "azure-issuer"

        const val IDENT = "01020312345"

        private val mockOAuth2Server: MockOAuth2Server by lazy {
            MockOAuth2Server().also { server ->
                server.start()
            }
        }

        fun issueTokenXToken(): String =
            mockOAuth2Server
                .issueToken(
                    issuerId = TOKEN_X_ISSUER_ID,
                    claims = mapOf("pid" to IDENT),
                ).serialize()

        fun issueAzureToken(): String = mockOAuth2Server.issueToken(AZURE_ISSUER_ID).serialize()

        @JvmStatic
        @BeforeAll
        fun setup() {
            System.setProperty("token-x.well-known-url", mockOAuth2Server.wellKnownUrl(TOKEN_X_ISSUER_ID).toString())
            System.setProperty("token-x.client-id", "default")

            System.setProperty("azure-app.well-known-url", mockOAuth2Server.wellKnownUrl(AZURE_ISSUER_ID).toString())
            System.setProperty("azure-app.client-id", "default")
        }

        @JvmStatic
        @AfterAll
        fun teardown() {
            System.clearProperty("token-x.well-known-url")
            System.clearProperty("token-x.client-id")
            System.clearProperty("azure-app.well-known-url")
            System.clearProperty("azure-app.client-id")
        }
    }

    @Test
    fun `test at bruker ikke har søknad`() =
        withMigratedDb {
            testApplication {
                application {
                    innsynApi(
                        PostgresPersonRepository(),
                        mockk(),
                    )
                }
                client.autentisert("/soknad").let { response ->
                    assertEquals(HttpStatusCode.OK, response.status)
                    assertEquals("[ ]", response.bodyAsText())
                }
            }
        }

    @Test
    fun `test at bruker har søknad`() =
        withMigratedDb {
            val personRepository =
                PostgresPersonRepository().also {
                    it.person(IDENT).also { person ->
                        person.håndter(
                            søknad("1", "1", LocalDateTime.now().minusDays(90), "Søknad om"),
                        )
                        person.håndter(
                            søknad("2", "11", LocalDateTime.now().minusDays(90)),
                        )
                        person.håndter(
                            søknad("3", "12", LocalDateTime.now().minusDays(90)),
                        )
                        person.håndter(
                            søknad("4", "13", LocalDateTime.now().minusDays(90)),
                        )
                        person.håndter(
                            søknad("5", "14", LocalDateTime.now().minusDays(90)),
                        )
                        person.håndter(Sakstilknytning("11", "arenaId"))
                        person.håndter(
                            Vedtak(
                                "2",
                                "arenaId",
                                Vedtak.Status.INNVILGET,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                null,
                            ),
                        )
                        it.lagre(person)
                    }
                }
            testApplication {
                application {
                    innsynApi(
                        personRepository,
                        mockk(),
                    )
                }
                val fom = LocalDate.now().minusDays(100)
                val dagensDato = LocalDate.now()
                client.autentisert("/soknad?soktFom=$fom&soktTom=$dagensDato").let { response ->
                    assertEquals(HttpStatusCode.OK, response.status)
                    response.bodyAsText().let { content ->
                        assertTrue(content.contains(NySøknad.toString()))
                        assertTrue(
                            content.contains(
                                Innsending.Vedlegg.Status.LastetOpp
                                    .toString(),
                            ),
                        )
                        assertTrue(content.contains("Søknad om"))
                    }
                }
            }
        }

    @Test
    fun `Søknad fra ny søknadsdialog får flagget erNySøknadsdialog satt til true`() =
        withMigratedDb {
            val søknadIdNyttFormat = UUID.randomUUID().toString()
            val personRepository =
                PostgresPersonRepository().also {
                    it.person(IDENT).also { person ->
                        person.håndter(
                            søknad(søknadIdNyttFormat, "11", LocalDateTime.now().minusDays(90)),
                        )
                        it.lagre(person)
                    }
                }
            testApplication {
                application {
                    innsynApi(
                        personRepository,
                        mockk(),
                    )
                }
                val fom = LocalDate.now().minusDays(100)
                val dagensDato = LocalDate.now()
                client.autentisert("/soknad?soktFom=$fom&soktTom=$dagensDato").let { response ->
                    ObjectMapper().readTree(response.bodyAsText()).let { jsonNode ->
                        val fraNySøknadsdialog = jsonNode[0]
                        val erNySøknadsdialog = fraNySøknadsdialog["erNySøknadsdialog"].asBoolean()
                        assertTrue(erNySøknadsdialog) { "Forventet at erNySøknadsdialog: true. $jsonNode" }

                        val url = "https://arbeid.intern.dev.nav.no/dagpenger/dialog/soknad/"
                        assertEquals("$url$søknadIdNyttFormat/kvittering", fraNySøknadsdialog["endreLenke"].asText())
                    }
                }
            }
        }

    @Test
    fun `test at bruker har søknad som ikke kommer med`() =
        withMigratedDb {
            val personRepository =
                PostgresPersonRepository().also {
                    it.person(IDENT).also { person ->
                        person.håndter(
                            søknad("1", "1", LocalDateTime.now().minusDays(90)),
                        )
                        person.håndter(Sakstilknytning("11", "arenaId"))
                        person.håndter(
                            Vedtak(
                                "2",
                                "arenaId",
                                Vedtak.Status.INNVILGET,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                null,
                            ),
                        )
                        it.lagre(person)
                    }
                }
            testApplication {
                application {
                    innsynApi(
                        personRepository,
                        mockk(),
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
    fun `test at bruker har vedtak`() =
        withMigratedDb {
            val personRepository =
                PostgresPersonRepository().also {
                    it.person(IDENT).also { person ->
                        person.håndter(
                            søknad(),
                        )
                        person.håndter(Sakstilknytning("11", "arenaId"))
                        person.håndter(
                            Vedtak(
                                "2",
                                "arenaId",
                                Vedtak.Status.INNVILGET,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                null,
                            ),
                        )
                        it.lagre(person)
                    }
                }
            testApplication {
                application {
                    innsynApi(
                        personRepository,
                        mockk(),
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
    fun `test at bruker har vedtak som ikke er innefor tiden`() =
        withMigratedDb {
            val personRepository =
                PostgresPersonRepository().also {
                    it.person(IDENT).also { person ->
                        person.håndter(
                            søknad(),
                        )
                        person.håndter(Sakstilknytning("11", "arenaId"))
                        person.håndter(
                            Vedtak(
                                "2",
                                "arenaId",
                                Vedtak.Status.INNVILGET,
                                LocalDateTime.now().minusDays(90),
                                LocalDateTime.now(),
                                null,
                            ),
                        )
                        it.lagre(person)
                    }
                }
            testApplication {
                application {
                    innsynApi(
                        personRepository,
                        mockk(),
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
    fun `test at bruker kan hente ut påbegynte søknader`() {
        val påbegyntOppslagMock = mockk<PåbegyntOppslag>()
        val nå = ZonedDateTime.now()
        val uuid = UUID.randomUUID()
        val påbegyntNySøknadsdialog =
            PåbegyntSøknadDto(
                uuid = uuid,
                opprettet = ZonedDateTime.of(LocalDateTime.MAX, ZoneId.of("Europe/Oslo")),
                sistEndret = nå,
            )
        coEvery { påbegyntOppslagMock.hentPåbegyntSøknad(any(), any()) } returns påbegyntNySøknadsdialog

        testApplication {
            application {
                innsynApi(
                    mockk<PostgresPersonRepository>(),
                    påbegyntOppslagMock,
                )
            }
            val response = client.autentisert("/paabegynte")
            assertEquals(HttpStatusCode.OK, response.status)
            val json = response.bodyAsText().let { objectMapper.readTree(it) }

            val fraNySøknadsdialog = json[0]
            assertEquals("Søknad om dagpenger", fraNySøknadsdialog["tittel"].asText())
            assertEquals(uuid.toString(), fraNySøknadsdialog["søknadId"].asText())
            assertTrue(fraNySøknadsdialog["erNySøknadsdialog"].asBoolean())
            assertEquals(
                nå.toLocalDateTime(),
                fraNySøknadsdialog["sistEndret"].asText().let { LocalDateTime.parse(it) },
            )
            assertEquals("https://arbeid.intern.dev.nav.no/dagpenger/dialog/soknad/$uuid", fraNySøknadsdialog["endreLenke"].asText())
        }
    }

    @Test
    fun `får behandlingsstatus FerdigBehandlet når det er 1 søknad og 1 vedtak`() =
        withMigratedDb {
            val personRepository =
                PostgresPersonRepository().also {
                    it.person(IDENT).also { person ->
                        person.håndter(
                            søknad(),
                        )
                        person.håndter(Sakstilknytning("11", "arenaId"))
                        person.håndter(
                            Vedtak(
                                "2",
                                "arenaId",
                                Vedtak.Status.INNVILGET,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                null,
                            ),
                        )
                        it.lagre(person)
                    }
                }
            testApplication {
                application {
                    innsynApi(
                        personRepository,
                        mockk(),
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
    fun `InternalServerError når man ikke kan parse fom dato`() =
        withMigratedDb {
            testApplication {
                application {
                    innsynApi(
                        PostgresPersonRepository(),
                        mockk(),
                    )
                }
                assertEquals(
                    HttpStatusCode.InternalServerError,
                    client.autentisert("/behandlingsstatus?fom=ugyldig_fom").status,
                )
                assertEquals(
                    HttpStatusCode.InternalServerError,
                    client.autentisert("/behandlingsstatus").status,
                )
            }
        }

    @Test
    fun `test at det ikke er mulig å hente søknader med POST uten Azure-token`() =
        withMigratedDb {
            testApplication {
                application {
                    innsynApi(
                        PostgresPersonRepository(),
                        mockk(),
                    )
                }
                val fom = LocalDate.now().minusDays(100)
                val dagensDato = LocalDate.now()

                val dataRequest =
                    DataRequest(
                        IDENT,
                        fom,
                        dagensDato,
                    )

                client
                    .autentisert(
                        "/soknad",
                        "",
                        HttpMethod.Post,
                        objectMapper.writeValueAsString(dataRequest),
                    ).let { response ->
                        assertEquals(HttpStatusCode.Unauthorized, response.status)
                    }
            }
        }

    @Test
    fun `test at det er mulig å hente søknader med POST med Azure-token`() =
        withMigratedDb {
            val søknadId = "1"
            val journalpostId = "2"
            val datoInnsendt = LocalDateTime.now().minusDays(90).truncatedTo(ChronoUnit.MICROS)

            val søknad = søknad(søknadId, journalpostId, datoInnsendt, "Søknad om")

            val personRepository =
                PostgresPersonRepository().also {
                    it.person(IDENT).also { person ->
                        person.håndter(
                            søknad,
                        )
                        it.lagre(person)
                    }
                }

            testApplication {
                application {
                    innsynApi(
                        personRepository,
                        mockk(),
                    )
                }
                val fom = LocalDate.now().minusDays(100)
                val dagensDato = LocalDate.now()

                val dataRequest =
                    DataRequest(
                        IDENT,
                        fom,
                        dagensDato,
                    )

                client
                    .autentisert(
                        "/soknad",
                        issueAzureToken(),
                        HttpMethod.Post,
                        objectMapper.writeValueAsString(dataRequest),
                    ).let { response ->
                        assertEquals(HttpStatusCode.OK, response.status)

                        val list = objectMapper.readValue<List<SoknadResponse>>(response.bodyAsText())
                        assertEquals(1, list.size)
                        assertEquals(søknadId, list[0].søknadId)
                        assertEquals(journalpostId, list[0].journalpostId)
                        assertEquals(datoInnsendt, list[0].datoInnsendt)
                        assertEquals(SoknadResponse.SøknadsType.NySøknad, list[0].søknadsType)
                        assertEquals(SoknadResponse.Kanal.Digital, list[0].kanal)
                        assertEquals("NAV 04-01.03", list[0].skjemaKode)
                        assertEquals(null, list[0].tittel)
                        assertEquals(null, list[0].endreLenke)
                        assertEquals(null, list[0].erNySøknadsdialog)
                        assertEquals(null, list[0].vedlegg)
                    }
            }
        }

    @Test
    fun `test at det ikke er mulig å hente vedtak med POST uten Azure-token`() =
        withMigratedDb {
            testApplication {
                application {
                    innsynApi(
                        PostgresPersonRepository(),
                        mockk(),
                    )
                }
                val fom = LocalDate.now().minusDays(100)
                val dagensDato = LocalDate.now()

                val dataRequest =
                    DataRequest(
                        IDENT,
                        fom,
                        dagensDato,
                    )

                client
                    .autentisert(
                        "/vedtak",
                        "",
                        HttpMethod.Post,
                        objectMapper.writeValueAsString(dataRequest),
                    ).let { response ->
                        assertEquals(HttpStatusCode.Unauthorized, response.status)
                    }
            }
        }

    @Test
    fun `test at det er mulig å hente vedtak med POST med Azure-token`() =
        withMigratedDb {
            val vedtakId = "1"
            val fagsakId = "arenaId"
            val datoFattet = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS)
            val fraDato = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MICROS)
            val tilDato = LocalDateTime.now().plusDays(100).truncatedTo(ChronoUnit.MICROS)

            val personRepository =
                PostgresPersonRepository().also {
                    it.person(IDENT).also { person ->
                        person.håndter(
                            Vedtak(
                                vedtakId,
                                fagsakId,
                                Vedtak.Status.INNVILGET,
                                datoFattet,
                                fraDato,
                                tilDato,
                            ),
                        )
                        it.lagre(person)
                    }
                }

            testApplication {
                application {
                    innsynApi(
                        personRepository,
                        mockk(),
                    )
                }
                val fom = LocalDate.now().minusDays(100)
                val dagensDato = LocalDate.now()

                val dataRequest =
                    DataRequest(
                        IDENT,
                        fom,
                        dagensDato,
                    )

                client
                    .autentisert(
                        "/vedtak",
                        issueAzureToken(),
                        HttpMethod.Post,
                        objectMapper.writeValueAsString(dataRequest),
                    ).let { response ->
                        assertEquals(HttpStatusCode.OK, response.status)

                        val list = objectMapper.readValue<List<VedtakResponse>>(response.bodyAsText())
                        assertEquals(1, list.size)
                        assertEquals(vedtakId, list[0].vedtakId)
                        assertEquals(fagsakId, list[0].fagsakId)
                        assertEquals(VedtakResponse.Status.INNVILGET, list[0].status)
                        assertEquals(datoFattet, list[0].datoFattet)
                        assertEquals(fraDato, list[0].fraDato)
                        assertEquals(tilDato, list[0].tilDato)
                    }
            }
        }

    private suspend fun HttpClient.autentisert(
        endepunkt: String,
        token: String = issueTokenXToken(),
        httpMethod: HttpMethod = HttpMethod.Get,
        body: String? = null,
    ): HttpResponse =
        this.request {
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

    private fun søknad(
        søknadId: String = "1",
        journalpostId: String = "1",
        datoInnsendt: LocalDateTime = LocalDateTime.now(),
        tittel: String? = null,
    ) = Søknad(
        søknadId = søknadId,
        journalpostId = journalpostId,
        skjemaKode = "NAV 04-01.03",
        søknadsType = NySøknad,
        kanal = Kanal.Digital,
        datoInnsendt = datoInnsendt,
        vedlegg = listOf(Innsending.Vedlegg("123", "navn", Innsending.Vedlegg.Status.LastetOpp)),
        tittel = tittel,
    )
}
