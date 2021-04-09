package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.Behandlingskjede
import no.nav.dagpenger.innsyn.modell.BehandlingskjedeId

abstract class Hendelse(
    val behandlingskjedeId: BehandlingskjedeId,
    oppgaver: Set<Oppgave>
) {
    internal val plan = Behandlingskjede(behandlingskjedeId, oppgaver)
}
