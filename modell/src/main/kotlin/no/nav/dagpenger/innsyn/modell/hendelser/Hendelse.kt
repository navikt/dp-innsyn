package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.Plan

abstract class Hendelse(
    val prosessId: String,
    oppgaver: List<Oppgave>
) {
    internal val plan = Plan(oppgaver)
}
