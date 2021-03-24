package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Mangelbrev
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.Status.Ferdig
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.Status.Uferdig
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak

class Søknadsprosess constructor(
    private val id: String,
    oppgaver: List<Oppgave>,
) {
    private val oppgaver = oppgaver.toMutableList()

    fun ferdigeOppgaverAv(type: OppgaveType) = oppgaver.filter { it.oppgaveType == type && it.status == Ferdig }
    fun uferdigeOppgaverAv(type: OppgaveType) = oppgaver.filter { it.oppgaveType == type && it.status == Uferdig }

    fun håndter(ettersending: Ettersending) {
        if (id != ettersending.søknadId) return

        oppgaver.forEach { it.håndter(ettersending) }
    }

    fun håndter(vedtak: Vedtak) {
        if (id != vedtak.søknadId) return

        oppgaver.forEach { it.håndter(vedtak) }
    }

    fun håndter(mangelbrev: Mangelbrev) {
        if (id != mangelbrev.søknadId) return

        oppgaver.addAll(mangelbrev.oppgaver)
    }
}
