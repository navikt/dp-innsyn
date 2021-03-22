package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.VedleggOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.VedtakOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PersonTest {
    @Test
    fun `Person får søknad under behandling etter ny søknad`() {
        Person("ident").also { person ->
            person.håndter(søknad("id1", manglerVedtakOppgave()))
            assertTrue(person.harSøknadUnderBehandling())
        }
    }

    @Test
    fun `Søknad uten vedlegg og deretter innsendte vedlegg`() {
        Person("ident").also { person ->
            person.håndter(søknad("id1", manglerEttersendingOppgave("vedleggA") + manglerVedtakOppgave()))
            assertTrue(person.harSøknadUnderBehandling())
            person.håndter(ettersending("vedleggA", manglerVedtakOppgave()))
            assertTrue(person.harSøknadUnderBehandling())
            assertTrue(person.harKomplettSøknad())
        }
    }

    private fun søknad(id: String, oppgaver: List<Oppgave>) = Søknad(id, oppgaver)
    private fun ettersending(id: String, oppgaver: List<Oppgave>) = Ettersending(id, oppgaver)
    private fun manglerVedtakOppgave() = listOf(Oppgave("vedtak", VedtakOppgave))
    private fun manglerEttersendingOppgave(navn: String) = listOf(Oppgave("id", VedleggOppgave))
}