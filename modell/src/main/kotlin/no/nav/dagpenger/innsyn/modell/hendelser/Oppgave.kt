package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.OppgaveType
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.Status.Ferdig
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.Status.Uferdig

open class Oppgave private constructor(private val id: String, type: OppgaveType, var status: Status) {

    constructor(id: String, type: OppgaveType): this(id, type, Uferdig)

    fun håndter(hendelse: Hendelse) {
        if(this !in hendelse.oppgaver) return

        hendelse.oppgaver.first{ it == this }.also {
            status = it.status
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is Oppgave && id == other.id && oppgaveType == other.oppgaveType
    }

    val oppgaveType = type

    enum class Status{
        Ferdig,
        Uferdig
    }

    class FerdigOppgave(id: String, type: OppgaveType): Oppgave(id, type, Ferdig){
    }
}





