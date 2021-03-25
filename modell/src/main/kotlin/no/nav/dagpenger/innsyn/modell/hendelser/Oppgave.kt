package no.nav.dagpenger.innsyn.modell.hendelser

import java.util.Objects

class Oppgave private constructor(
    private val id: String,
    val oppgaveType: OppgaveType,
    var tilstand: Tilstand
) {
    override fun equals(other: Any?): Boolean {
        return other is Oppgave && id == other.id && oppgaveType == other.oppgaveType
    }

    override fun hashCode(): Int {
        return Objects.hash(id, oppgaveType)
    }

    interface Tilstand

    object Uferdig : Tilstand
    object Ferdig : Tilstand

    class OppgaveType(private val type: String) {
        fun ny(id: String) = Oppgave(id, this, Oppgave.Uferdig)
        fun ferdig(id: String) = Oppgave(id, this, Oppgave.Ferdig)
    }
}
