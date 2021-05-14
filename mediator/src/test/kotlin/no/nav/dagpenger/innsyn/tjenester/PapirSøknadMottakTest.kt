package no.nav.dagpenger.innsyn.tjenester

import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.dagpenger.innsyn.melding.PapirSøknadsMelding
import no.nav.dagpenger.innsyn.modell.hendelser.PapirSøknad
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PapirSøknadMottakTest {

    private val testRapid = TestRapid()
    private val personMediator = mockk<PersonMediator>(relaxed = true)

    @BeforeEach
    internal fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `vi kan motta søknad`() {
        PapirSøknadMottak(testRapid, personMediator)
        testRapid.sendTestMessage(papirsøknadJson)
        verify { personMediator.håndter(any<PapirSøknad>(), any<PapirSøknadsMelding>()) }
        confirmVerified(personMediator)
    }
}

private val papirsøknadJson = """{
  "@id": "123",
  "@opprettet": "2021-01-01T01:01:01.000001",
  "journalpostId": "12455",
  "datoRegistrert": "2021-01-01T01:01:01.000001",
  "type": "NySøknad",
  "fødselsnummer": "11111111111",
  "aktørId": "1234455",
  "søknadsData": {},
  "@event_name": "innsending_mottatt",
  "system_read_count": 0
}
""".trimIndent()
