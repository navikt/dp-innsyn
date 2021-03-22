package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.Status.Ferdig
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.Status.Uferdig
import no.nav.dagpenger.innsyn.modell.hendelser.OppgaveType.Companion.vedlegg
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak

class Søknadsprosess constructor(
    private val id: String,
    oppgaver: List<Oppgave>,
) {
    private val oppgaver = oppgaver.toMutableList()

    fun harUferdigeOppgaver() = oppgaver.any { it.status == Uferdig }

    fun erKomplett() = oppgaver.any { it.oppgaveType == vedlegg && it.status == Ferdig }

    fun håndter(ettersending: Ettersending) {
        if (id != ettersending.søknadId) return

        oppgaver.forEach { it.håndter(ettersending) }
    }

    fun håndter(vedtak: Vedtak) {
        if (id != vedtak.søknadId) return

        oppgaver.forEach { it.håndter(vedtak) }
    }
}
