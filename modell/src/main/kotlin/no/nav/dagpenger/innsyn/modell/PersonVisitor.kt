package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import java.time.LocalDateTime

interface PersonVisitor : PlanVisitor {
    fun preVisit(person: Person, fnr: String) {}
    fun postVisit(person: Person, fnr: String) {}
}

interface PlanVisitor : OppgaveVisitor {
    fun preVisit(plan: Plan) {}
    fun postVisit(plan: Plan) {}
}

interface OppgaveVisitor {
    fun preVisit(
        oppgave: Oppgave,
        id: String,
        beskrivelse: String,
        opprettet: LocalDateTime,
        oppgaveType: Oppgave.OppgaveType,
        tilstand: OppgaveTilstand
    ) {}
    fun postVisit(
        oppgave: Oppgave,
        id: String,
        beskrivelse: String,
        opprettet: LocalDateTime,
        oppgaveType: Oppgave.OppgaveType,
        tilstand: OppgaveTilstand
    ) {}
}