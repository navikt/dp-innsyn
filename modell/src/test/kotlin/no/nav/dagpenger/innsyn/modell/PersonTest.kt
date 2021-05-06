package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Mangelbrev
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveId
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class PersonTest {
    @Test
    fun `Person får søknad under behandling etter ny søknad`() {
        Person("ident").also { person ->
            person.håndter(søknad("id1", manglerVedtakOppgave()))
            assertEquals(1, PersonInspektør(person).uferdigeOppgaver)
        }
    }

    @Test
    fun `Flere søknader gir flere stønadsforhold`() {
        Person("ident").also { person ->
            person.håndter(søknad("id1", manglerVedleggOppgave("vedleggA") + manglerVedtakOppgave()))
            person.håndter(søknad("id2", manglerVedleggOppgave("vedleggA") + manglerVedtakOppgave()))

            assertEquals(2, PersonInspektør(person).antallStønadsforhold)
        }
    }

    @Test
    fun `Søknad uten vedlegg og deretter innsendte vedlegg `() {
        Person("ident").also { person ->
            person.håndter(søknad("id", manglerVedleggOppgave("vedleggA")))

            assertEquals(1, PersonInspektør(person).uferdigeOppgaver)

            person.håndter(ettersending("id", ferdigVedleggOppgave("vedleggB")))
            assertEquals(1, PersonInspektør(person).ferdigeOppgaver)
            assertEquals(1, PersonInspektør(person).uferdigeOppgaver)

            person.håndter(ettersending("id", ferdigVedleggOppgave("vedleggA")))
            assertEquals(2, PersonInspektør(person).ferdigeOppgaver)
            assertEquals(0, PersonInspektør(person).uferdigeOppgaver)
        }
    }

    @Test
    fun `Person skal kunne motta mangelbrev`() {
        Person("ident").also { person ->
            person.håndter(søknad("id", manglerVedtakOppgave()))
            person.håndter(mangelbrev("id"))
            assertEquals(1, PersonInspektør(person).uferdigeOppgaver)
        }
    }

    private fun søknad(id: String, oppgaver: Set<Oppgave>) = Søknad(id, "journalpostId", oppgaver)
    private fun ettersending(id: String, oppgaver: Set<Oppgave>) = Ettersending(id, oppgaver)
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
        var antallStønadsforhold = 0

        init {
            person.accept(this)
        }

        override fun postVisit(
            søknadsprosess: Søknadsprosess,
            tilstand: Søknadsprosess.Tilstand
        ) {
            antallStønadsforhold++
        }

        override fun preVisit(
            oppgave: Oppgave,
            id: OppgaveId,
            beskrivelse: String,
            opprettet: LocalDateTime,
            tilstand: OppgaveTilstand
        ) {
            when (tilstand) {
                OppgaveTilstand.Uferdig -> uferdigeOppgaver++
                OppgaveTilstand.Ferdig -> ferdigeOppgaver++
            }
        }
    }
}
