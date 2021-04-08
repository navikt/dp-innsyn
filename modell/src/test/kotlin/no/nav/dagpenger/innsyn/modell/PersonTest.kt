package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Mangelbrev
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class PersonTest {
    @Test
    fun `Person får søknad under behandling etter ny søknad`() {
        Person("ident").also { person ->
            person.håndter(søknad("id1", manglerVedtakOppgave()))
            assertTrue(person.harUferdigeOppgaverAv(vedtakOppgave))
        }
    }

    @Test
    fun `Flere søknader gir flere behandlingskjeder`() {
        Person("ident").also { person ->
            person.håndter(søknad("id1", manglerVedleggOppgave("vedleggA") + manglerVedtakOppgave()))
            person.håndter(søknad("id2", manglerVedleggOppgave("vedleggA") + manglerVedtakOppgave()))

            assertEquals(2, PersonInspektør(person).antallBehandlingskjeder)
        }
    }

    @Test
    fun `Søknad uten vedlegg og deretter innsendte vedlegg også vedtak`() {
        Person("ident").also { person ->
            person.håndter(søknad("id", manglerVedleggOppgave("vedleggA") + manglerVedtakOppgave()))
            assertTrue(person.harUferdigeOppgaverAv(vedtakOppgave))
            assertTrue(person.harUferdigeOppgaverAv(vedleggOppgave))

            person.håndter(ettersending("id", ferdigVedleggOppgave("vedleggB")))
            assertTrue(person.harUferdigeOppgaverAv(vedleggOppgave))

            person.håndter(ettersending("id", ferdigVedleggOppgave("vedleggA")))
            assertTrue(person.harUferdigeOppgaverAv(vedtakOppgave))
            assertFalse(person.harUferdigeOppgaverAv(vedleggOppgave))

            person.håndter(vedtak("id"))
            assertFalse(person.harUferdigeOppgaverAv(vedtakOppgave))
        }
    }

    @Test
    fun `Person skal kunne motta mangelbrev`() {
        Person("ident").also { person ->
            person.håndter(søknad("id", manglerVedtakOppgave()))
            person.håndter(mangelbrev("id"))
            assertTrue(person.harUferdigeOppgaverAv(mangelbrevOppgave))
        }
    }

    private fun søknad(id: String, oppgaver: Set<Oppgave>) = Søknad(id, oppgaver)
    private fun ettersending(id: String, oppgaver: Set<Oppgave>) = Ettersending(id, oppgaver)
    private fun vedtak(søknadId: String) = Vedtak("1", søknadId, setOf(vedtakOppgave.ferdig("vedtak", "")))
    private fun mangelbrev(søknadId: String) = Mangelbrev("id", søknadId, setOf(mangelbrevOppgave.ny("", "")))
    private fun manglerVedtakOppgave() = setOf(vedtakOppgave.ny("vedtak", ""))
    private fun manglerVedleggOppgave(navn: String) = setOf(vedleggOppgave.ny(navn, ""))
    private fun ferdigVedleggOppgave(navn: String) = setOf(vedleggOppgave.ferdig(navn, ""))

    private val vedtakOppgave = OppgaveType("testOppgaveVedtak")
    private val vedleggOppgave = OppgaveType("testOppgaveVedlegg")
    private val mangelbrevOppgave = OppgaveType("testOppgaveMangelbrev")

    private class PersonInspektør(person: Person) : PersonVisitor {
        var uferdigeOppgaver = 0
        var ferdigeOppgaver = 0
        val antallBehandlingskjeder get() = behandlingskjeder.size
        private val behandlingskjeder = mutableSetOf<BehandlingskjedeId>()

        init {
            person.accept(this)
        }

        override fun preVisit(
            oppgave: Oppgave,
            id: String,
            beskrivelse: String,
            opprettet: LocalDateTime,
            oppgaveType: OppgaveType,
            tilstand: OppgaveTilstand
        ) {
            behandlingskjeder.add(id)
            when (tilstand) {
                OppgaveTilstand.Uferdig -> uferdigeOppgaver++
                OppgaveTilstand.Ferdig -> ferdigeOppgaver++
            }
        }
    }
}
