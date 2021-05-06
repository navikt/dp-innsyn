package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import org.junit.jupiter.api.Test

internal class EttersendingTest {

    @Test
    fun `Når man sender inn en ettersedning skal man ikke lenger ha uferdige oppgaver`() {
        Person("ident").also { person ->
            person.håndter(søknad("id1", manglerEttersendingOppgave()))
            // assertTrue(person.harUferdigeOppgaverAv(testOppgave))

            person.håndter(ettersending("id1", setOf(testOppgave.ferdig("1", ""))))
            // assertFalse(person.harUferdigeOppgaverAv(testOppgave))
        }
    }

    private fun søknad(id: String, oppgaver: Set<Oppgave>) = Søknad(id, "journalpostId", oppgaver)
    private fun ettersending(id: String, oppgaver: Set<Oppgave>) = Ettersending(id, oppgaver)
    private fun manglerEttersendingOppgave() = setOf(testOppgave.ny("1", ""))

    val testOppgave = OppgaveType("testOppgave")
}
