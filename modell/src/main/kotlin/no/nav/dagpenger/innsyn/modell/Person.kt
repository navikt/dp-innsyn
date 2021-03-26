package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Hendelse
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand.Ferdig
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand.Uferdig
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor

class Person private constructor(
    val fnr: String,
    oppgaver: Collection<Oppgave>
) {
    constructor(fnr: String) : this(fnr, emptySet())

    private var plan = Plan(oppgaver.toSet())

    fun harUferdigeOppgaverAv(type: OppgaveType) = oppgaverAv(type, Uferdig).isNotEmpty()
    fun harFerdigeOppgaverAv(type: OppgaveType) = oppgaverAv(type, Ferdig).isNotEmpty()

    fun håndter(hendelse: Hendelse) {
        plan = plan.slåSammen(hendelse.plan)
    }

    private fun oppgaverAv(type: OppgaveType, oppgaveTilstand: Oppgave.OppgaveTilstand): Set<Oppgave> =
        mutableSetOf<Oppgave>().also { oppgaver ->
            plan.forEach { it.leggTilHvis(type, oppgaveTilstand, oppgaver) }
        }

    fun accept(visitor: PersonVisitor) {
        visitor.preVisit(this, fnr)
        plan.accept(visitor)
        visitor.postVisit(this, fnr)
    }
}
