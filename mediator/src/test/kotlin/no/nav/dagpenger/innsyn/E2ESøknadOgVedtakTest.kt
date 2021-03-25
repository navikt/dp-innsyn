package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.Dagpenger.vedtakOppgave
import no.nav.dagpenger.innsyn.helpers.InMemoryPersonRepository
import no.nav.dagpenger.innsyn.tjenester.EttersendingMottak
import no.nav.dagpenger.innsyn.tjenester.SøknadMottak
import no.nav.dagpenger.innsyn.tjenester.VedtakMottak
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class E2ESøknadOgVedtakTest {
    private val rapid = TestRapid()
    private val personRepository = InMemoryPersonRepository()
    private val personMediator = PersonMediator(personRepository)
    private val søknadAsJson = javaClass.getResource("/soknadsdata.json").readText()
    private val ettersendingAsJson = javaClass.getResource("/ettersending.json").readText()
    private val vedtakAsJson = javaClass.getResource("/vedtak.json").readText()

    init {
        SøknadMottak(rapid, personMediator)
        EttersendingMottak(rapid, personMediator)
        VedtakMottak(rapid, personMediator)
    }

    @Test
    fun `skal kunne motta søknad og vedtak`() {
        val person = personRepository.person("10108099999")

        rapid.sendTestMessage(søknadAsJson)
        assertTrue(person.harUferdigeOppgaverAv(vedtakOppgave))

        rapid.sendTestMessage(ettersendingAsJson)
        assertTrue(person.harUferdigeOppgaverAv(vedtakOppgave))

        rapid.sendTestMessage(vedtakAsJson)
        assertFalse(person.harUferdigeOppgaverAv(vedtakOppgave))
    }
}
