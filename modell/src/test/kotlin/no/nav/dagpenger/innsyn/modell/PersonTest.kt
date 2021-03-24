package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.OppgaveType.Companion.vedlegg
import no.nav.dagpenger.innsyn.modell.hendelser.OppgaveType.Companion.vedtak
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import org.junit.jupiter.api.Assertions.assertFalse
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
    fun `Søknad uten vedlegg og deretter innsendte vedlegg også vedtak`() {
        Person("ident").also { person ->
            person.håndter(søknad("id", manglerVedleggOppgave("vedleggA") + manglerVedtakOppgave()))
            assertTrue(person.harSøknadUnderBehandling())

            person.håndter(ettersending("id", manglerVedleggOppgave("vedleggB")))
            assertFalse(person.harKomplettSøknad())

            person.håndter(ettersending("id", manglerVedleggOppgave("vedleggA")))
            assertTrue(person.harSøknadUnderBehandling())
            assertTrue(person.harKomplettSøknad())

            person.håndter(vedtak("id"))
            assertFalse(person.harSøknadUnderBehandling())
        }
    }

    private fun søknad(id: String, oppgaver: List<Oppgave>) = Søknad(id, oppgaver)
    private fun ettersending(id: String, oppgaver: List<Oppgave>) = Ettersending(id, oppgaver)
    private fun vedtak(søknadId: String) = Vedtak("1", søknadId)
    private fun manglerVedtakOppgave() = listOf(Oppgave("vedtak", vedtak))
    private fun manglerVedleggOppgave(navn: String) = listOf(Oppgave(navn, vedlegg))
}
