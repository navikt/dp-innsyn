package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.serde.PlanVisitor

class Plan internal constructor(
    private val oppgaver: Set<Oppgave>
) : Collection<Oppgave> by oppgaver {
    fun slåSammen(med: Plan) = Plan((oppgaver subtract med.oppgaver) + med.oppgaver)

    fun accept(visitor: PlanVisitor) {
        visitor.preVisit(this)
        oppgaver.forEach { it.accept(visitor) }
        visitor.postVisit(this)
    }
}
