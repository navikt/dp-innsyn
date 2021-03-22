package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.modell.OppgaveType

object Dagpenger {
    val vedlegg = OppgaveType("Vedlegg")
    val ekstrating = OppgaveType("Mangelbrevet sier at du mangler noe")
    val vedtak = OppgaveType("Få vedtak")
}
