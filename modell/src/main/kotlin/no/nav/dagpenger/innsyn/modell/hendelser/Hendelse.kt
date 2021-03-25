package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.Plan

abstract class Hendelse(
    oppgaver: Set<Oppgave>
) {
    internal val plan = Plan(oppgaver)
}
