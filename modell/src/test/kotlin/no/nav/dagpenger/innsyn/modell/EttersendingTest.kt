package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.VedleggOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class EttersendingTest {

    @Test
    fun `Når man sender inn en ettersedning skal man ikke lenger ha uferdige oppgaver`() {
        Person("ident").also { person ->
            person.håndter(søknad("id1", manglerEttersendingOppgave()))
            Assertions.assertTrue(person.harSøknadUnderBehandling())

            person.håndter(ettersending("id1", listOf(Oppgave("1", VedleggOppgave))))
            assertFalse(person.harSøknadUnderBehandling())

        }
    }

    private fun søknad(id: String, oppgaver: List<Oppgave>) = Søknad(id, oppgaver)
    private fun ettersending(id: String, oppgaver: List<Oppgave>) = Ettersending(id, oppgaver)
    private fun manglerEttersendingOppgave() = listOf(Oppgave("1", VedleggOppgave))
}