package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.BehandlingskjedeId

abstract class Innsending(behandlingskjedeId: BehandlingskjedeId, oppgaver: Set<Oppgave>) : Hendelse(
    behandlingskjedeId,
    oppgaver
)
