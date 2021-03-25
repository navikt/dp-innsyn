package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType

object Dagpenger {
    val søknadOppgave = OppgaveType("Søke om dagpenger")
    val vedleggOppgave = OppgaveType("Vedlegg")
    val vedtakOppgave = OppgaveType("Få vedtak")
}
