package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Hendelse
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.serde.BehandlingskjedeVisitor

typealias BehandlingskjedeId = String

class Behandlingskjede internal constructor(
    private val id: BehandlingskjedeId,
    private var oppgaver: Set<Oppgave>
) : Collection<Oppgave> {
    override val size: Int get() = oppgaver.size
    fun håndter(hendelse: Hendelse) = erRelevant(hendelse).also {
        if (!it) return it

        oppgaver = slåSammen(hendelse.plan)
    }

    fun accept(visitor: BehandlingskjedeVisitor) {
        visitor.preVisit(this, id)
        oppgaver.forEach { it.accept(visitor) }
        visitor.postVisit(this, id)
    }

    private fun erRelevant(hendelse: Hendelse) = hendelse.behandlingskjedeId == id

    private fun slåSammen(med: Behandlingskjede) = (oppgaver subtract med.oppgaver) + med.oppgaver

    override fun contains(element: Oppgave) = oppgaver.contains(element)
    override fun containsAll(elements: Collection<Oppgave>) = oppgaver.containsAll(elements)
    override fun isEmpty() = oppgaver.isEmpty()
    override fun iterator() = oppgaver.iterator()
}
