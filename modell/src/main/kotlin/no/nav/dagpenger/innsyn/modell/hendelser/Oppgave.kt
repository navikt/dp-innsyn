package no.nav.dagpenger.innsyn.modell.hendelser

class Oppgave private constructor(
    private val id: String,
    val oppgaveType: OppgaveType,
    var tilstand: Tilstand
) {
    fun håndter(hendelse: Hendelse) {
        if (this !in hendelse.plan) return

        hendelse.plan.first { it == this }.also {
            tilstand = it.tilstand
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is Oppgave && id == other.id && oppgaveType == other.oppgaveType
    }

    interface Tilstand

    object Uferdig : Tilstand
    object Ferdig : Tilstand

    class OppgaveType(private val type: String) {
        fun ny(id: String) = Oppgave(id, this, Oppgave.Uferdig)
        fun ferdig(id: String) = Oppgave(id, this, Oppgave.Ferdig)
    }
}
