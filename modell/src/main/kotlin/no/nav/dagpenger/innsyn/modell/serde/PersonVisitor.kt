package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.Stønadsforhold
import no.nav.dagpenger.innsyn.modell.Stønadsid
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import java.time.LocalDateTime

interface PersonVisitor : StønadsforholdVisitor {
    fun preVisit(person: Person, fnr: String) {}
    fun postVisit(person: Person, fnr: String) {}
}


interface StønadsforholdVisitor : OppgaveVisitor, StønadsidVisitor {
    fun preVisit(
        stønadsforhold: Stønadsforhold,
        tilstand: Stønadsforhold.Tilstand
    ) {
    }

    fun postVisit(
        stønadsforhold: Stønadsforhold,
        tilstand: Stønadsforhold.Tilstand
    ) {
    }
}

interface StønadsidVisitor{
    fun preVisit(
        stønadsid: Stønadsid,
        internId: String,
        eksternId: String
    ){}
    fun postVisit(
        stønadsid: Stønadsid,
        internId: String,
        eksternId: String
    ){}
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