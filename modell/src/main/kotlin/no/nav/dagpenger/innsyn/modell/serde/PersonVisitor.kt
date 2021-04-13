package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Behandlingskjede
import no.nav.dagpenger.innsyn.modell.BehandlingskjedeId
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.Stønadsforhold
import no.nav.dagpenger.innsyn.modell.hendelser.Hendelse
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import java.time.LocalDateTime

interface PersonVisitor : BehandlingskjedeVisitor {
    fun preVisit(person: Person, fnr: String) {}
    fun postVisit(person: Person, fnr: String) {}
}

interface BehandlingskjedeVisitor : OppgaveVisitor {
    fun preVisit(behandlingskjede: Behandlingskjede, id: BehandlingskjedeId) {}
    fun postVisit(behandlingskjede: Behandlingskjede, id: BehandlingskjedeId) {}
}

interface OppgaveVisitor {
    fun preVisit(
        oppgave: Oppgave,
        id: Oppgave.OppgaveId,
        beskrivelse: String,
        opprettet: LocalDateTime,
        tilstand: OppgaveTilstand
    ) {
    }

    fun postVisit(
        oppgave: Oppgave,
        id: Oppgave.OppgaveId,
        beskrivelse: String,
        opprettet: LocalDateTime,
        tilstand: OppgaveTilstand
    ) {
    }
}

interface StønadsforholdVisitor : HendelseVisitor, OppgaveVisitor {
    fun preVisit(
        stønadsforhold: Stønadsforhold,
        tilstand: Stønadsforhold.Tilstand,
        opprettet: LocalDateTime,
        oppdatert: LocalDateTime
    ) {}
    fun postVisit(
        stønadsforhold: Stønadsforhold,
        tilstand: Stønadsforhold.Tilstand,
        opprettet: LocalDateTime,
        oppdatert: LocalDateTime
    ) {}
}

interface HendelseVisitor {
    fun preVisit(hendelse: Hendelse, behandlingskjedeId: BehandlingskjedeId) {}
    fun postVisit(hendelse: Hendelse, behandlingskjedeId: BehandlingskjedeId) {}
}
