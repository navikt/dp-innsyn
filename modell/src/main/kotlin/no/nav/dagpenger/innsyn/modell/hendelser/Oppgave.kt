package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.Status.Ferdig
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.Status.Uferdig

class Oppgave(private val id: String, type: OppgaveType) {
    fun håndter(ettersending: Ettersending) {
        if (this in ettersending.oppgaver) {
            status = Ferdig
        }
    }

    fun håndter(vedtak: Vedtak) {
        if (oppgaveType == OppgaveType.vedtak) {
            status = Ferdig
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is Oppgave && id == other.id && oppgaveType == other.oppgaveType
    }

    var status: Status = Uferdig
    val oppgaveType = type

    enum class Status {
        Ferdig,
        Uferdig
    }
}

class OppgaveType private constructor(type: String) {
    companion object {
        val vedlegg = OppgaveType("Vedlegg")
        val vedtak = OppgaveType("Få vedtak")
    }
}
