package no.nav.dagpenger.innsyn.tjenester

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.dagpenger.innsyn.melding.LegacySøknadsmelding
import no.nav.dagpenger.innsyn.melding.PapirSøknadsMelding
import no.nav.dagpenger.innsyn.melding.QuizSøknadMelding
import no.nav.dagpenger.innsyn.melding.SøknadMelding
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class SøknadMottakTest {
    private val testRapid = TestRapid()
    private val personMediator = mockk<PersonMediator>(relaxed = true)
    private val søknadMeldingSlot = slot<SøknadMelding>()
    private val meterRegistry: MeterRegistry = SimpleMeterRegistry()

    init {
        SøknadMottak(testRapid, personMediator)
    }

    @BeforeEach
    internal fun setUp() {
        testRapid.reset()
        søknadMeldingSlot.clear()
    }

    @Test
    fun `vi kan motta søknader`() {
        testRapid.sendTestMessage(søknadJson)
        verify { personMediator.håndter(any(), capture(søknadMeldingSlot)) }
        confirmVerified(personMediator)
        assertTrue(søknadMeldingSlot.isCaptured)
        assertEquals(søknadMeldingSlot.captured.javaClass.name, LegacySøknadsmelding::class.java.name)

        meterRegistry
            .getSampleValue(
                "dagpenger_mottak_forsinkelse_sum",
                "type" to "soknad",
            )?.let {
                assertTrue(SYNTHETIC_DELAY_SECONDS <= it)
            }
        meterRegistry
            .getSampleValue(
                "dagpenger_mottak_forsinkelse_count",
                "type" to "soknad",
            )?.let {
                assertTrue(1.0 <= it)
            }
    }

    @Test
    fun `vi kan motta papirsøknad`() {
        testRapid.sendTestMessage(papirsøknadJson)
        verify { personMediator.håndter(any(), capture(søknadMeldingSlot)) }
        confirmVerified(personMediator)
        assertTrue(søknadMeldingSlot.isCaptured)
        assertEquals(søknadMeldingSlot.captured.javaClass.name, PapirSøknadsMelding::class.java.name)
    }

    @Test
    fun `vi kan motta søknad fra ny søknadssdialog (quiz-format)`() {
        testRapid.sendTestMessage(søknadJsonFraNyQuiz)
        verify { personMediator.håndter(any(), capture(søknadMeldingSlot)) }
        confirmVerified(personMediator)
        assertTrue(søknadMeldingSlot.isCaptured)
        assertEquals(søknadMeldingSlot.captured.javaClass.name, QuizSøknadMelding::class.java.name)
    }
}

private fun MeterRegistry.getSampleValue(
    name: String,
    vararg labels: Pair<String, String>,
): Double? =
    find(name)
        .tags(*labels.flatMap { listOf(it.first, it.second) }.toTypedArray())
        .timer()
        ?.totalTime(java.util.concurrent.TimeUnit.SECONDS)

private const val SYNTHETIC_DELAY_SECONDS: Long = 5

@Language("JSON")
private val søknadJson =
    """
    {
      "@event_name": "innsending_mottatt",
      "@opprettet": "${LocalDateTime.now()}",
      "fødselsnummer": "123",
      "journalpostId": "123",
      "skjemaKode": "NAV 03-102.23",
      "tittel": "Tittel",
      "type": "NySøknad",
      "datoRegistrert": "${LocalDateTime.now().minusSeconds(SYNTHETIC_DELAY_SECONDS)}",
      "søknadsData": {
        "brukerBehandlingId": "123",
        "vedlegg": [],
        "skjemaNummer": "NAV12"
      }
    }
    """.trimIndent()

@Language("JSON")
private val papirsøknadJson =
    """
    {
      "@id": "123",
      "@opprettet": "2021-01-01T01:01:01.000001",
      "journalpostId": "12455",
      "datoRegistrert": "2021-01-01T01:01:01.000001",
      "skjemaKode": "NAV 03-102.23",
      "tittel": "Tittel",
      "type": "NySøknad",
      "fødselsnummer": "11111111111",
      "aktørId": "1234455",
      "søknadsData": {},
      "@event_name": "innsending_mottatt",
      "system_read_count": 0
    }
    """.trimIndent()

@Language("JSON")
private val søknadJsonFraNyQuiz =
    """
    {
      "@event_name": "innsending_mottatt",
      "@opprettet": "${LocalDateTime.now()}",
      "fødselsnummer": "123",
      "journalpostId": "123",
      "skjemaKode": "NAV 03-102.23",
      "tittel": "Tittel",
      "type": "NySøknad",
      "datoRegistrert": "${LocalDateTime.now().minusSeconds(SYNTHETIC_DELAY_SECONDS)}",
      "søknadsData": {
        "søknad_uuid": "${UUID.randomUUID()}"
      }
    }
    """.trimIndent()
