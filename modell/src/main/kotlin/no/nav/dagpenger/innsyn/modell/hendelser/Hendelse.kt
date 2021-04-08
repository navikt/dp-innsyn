package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.BehandlingskjedeId
import no.nav.dagpenger.innsyn.modell.Plan

abstract class Hendelse(
    val behandlingskjedeId: BehandlingskjedeId,
    oppgaver: Set<Oppgave>
) {
    internal val plan = Plan(behandlingskjedeId, oppgaver)
}
