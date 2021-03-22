package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave

class Søknadsprosess constructor(
    private val id: String,
    oppgaver: List<Oppgave>,
) {
    private val oppgaver = oppgaver.toMutableList()

    fun harUferdigeOppgaver() = oppgaver.any { it.status == "Uferdig" }

    fun håndter(ettersending: Ettersending) {
        if (id != ettersending.søknadId) return

        oppgaver.forEach { it.status = "Ferdig" }
    }
}
