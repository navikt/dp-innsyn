package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.serde.OppgaveVisitor
import java.time.LocalDateTime
import java.util.Objects

class Oppgave private constructor(
    private val id: String,
    private val beskrivelse: String,
    private val opprettet: LocalDateTime,
    private val oppgaveType: OppgaveType,
    private var tilstand: Tilstand
) {
    override fun equals(other: Any?): Boolean {
        return other is Oppgave && id == other.id && oppgaveType == other.oppgaveType
    }

    override fun hashCode(): Int {
        return Objects.hash(id, oppgaveType)
    }

    fun leggTilHvis(type: OppgaveType, oppgaveTilstand: OppgaveTilstand, oppgaver: MutableSet<Oppgave>) {
        if (oppgaveType == type && tilstand.kode == oppgaveTilstand) oppgaver.add(this)
    }

    internal fun accept(visitor: OppgaveVisitor) {
        visitor.preVisit(this, id, beskrivelse, opprettet, oppgaveType, tilstand.kode)
        visitor.postVisit(this, id, beskrivelse, opprettet, oppgaveType, tilstand.kode)
    }

    internal interface Tilstand {
        val kode: OppgaveTilstand
    }

    internal object Uferdig : Tilstand {
        override val kode = OppgaveTilstand.Uferdig
    }

    internal object Ferdig : Tilstand {
        override val kode = OppgaveTilstand.Ferdig
    }

    enum class OppgaveTilstand {
        Uferdig,
        Ferdig
    }

    class OppgaveType(private val type: String) {
        fun ny(id: String, beskrivelse: String) = Oppgave(id, beskrivelse, LocalDateTime.now(), this, Uferdig)
        fun ferdig(id: String, beskrivelse: String) = Oppgave(id, beskrivelse, LocalDateTime.now(), this, Ferdig)

        override fun equals(other: Any?) = other is OppgaveType && type == other.type
        override fun toString() = type
    }
}
