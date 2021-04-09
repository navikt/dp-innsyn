package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.serde.OppgaveVisitor
import java.time.LocalDateTime
import java.util.Objects

class Oppgave private constructor(
    private val id: OppgaveId,
    private val beskrivelse: String,
    private val opprettet: LocalDateTime,
    private var tilstand: Tilstand
) {
    override fun equals(other: Any?) = other is Oppgave && id == other.id
    override fun hashCode() = id.hashCode()

    fun leggTilHvis(type: OppgaveType, oppgaveTilstand: OppgaveTilstand, oppgaver: MutableSet<Oppgave>) {
        if (id.type == type && tilstand.kode == oppgaveTilstand) oppgaver.add(this)
    }

    internal fun accept(visitor: OppgaveVisitor) {
        visitor.preVisit(this, id, beskrivelse, opprettet, tilstand.kode)
        visitor.postVisit(this, id, beskrivelse, opprettet, tilstand.kode)
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

    class OppgaveId(val id: String, val type: OppgaveType, val indeks: Int = 0) {
        override fun equals(other: Any?) = other is OppgaveId && id == other.id && type == other.type
        override fun hashCode() = Objects.hash(id, type)
        override fun toString() = "$type:$id"
    }

    class OppgaveType(private val type: String) {
        fun ny(id: String, beskrivelse: String) =
            Oppgave(OppgaveId(id, this), beskrivelse, LocalDateTime.now(), Uferdig)

        fun ferdig(id: String, beskrivelse: String) =
            Oppgave(OppgaveId(id, this), beskrivelse, LocalDateTime.now(), Ferdig)

        override fun equals(other: Any?) = other is OppgaveType && type == other.type
        override fun toString() = type
    }
}
