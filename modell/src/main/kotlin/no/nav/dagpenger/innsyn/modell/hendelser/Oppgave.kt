package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.Vedlegg

class Oppgave(id: String, type: OppgaveType) {
    var status: String = "Uferdig"
    val oppgaveType = type

    enum class OppgaveType{
        VedleggOppgave,
        VedtakOppgave
    }

}



