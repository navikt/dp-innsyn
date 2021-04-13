package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.Behandlingskjede
import no.nav.dagpenger.innsyn.modell.BehandlingskjedeId
import no.nav.dagpenger.innsyn.modell.serde.HendelseVisitor

abstract class Hendelse(
    val behandlingskjedeId: BehandlingskjedeId,
    oppgaver: Set<Oppgave>
) {
    internal val plan = Behandlingskjede(behandlingskjedeId, oppgaver)

    fun accept(visitor: HendelseVisitor) {
        visitor.preVisit(this, behandlingskjedeId)
        visitor.postVisit(this, behandlingskjedeId)
    }
}
