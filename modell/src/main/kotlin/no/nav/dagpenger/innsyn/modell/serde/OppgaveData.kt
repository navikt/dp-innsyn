package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Stønadsid
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveId
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType
import java.time.LocalDateTime
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class OppgaveData(
    indeks: Int,
    id: String,
    beskrivelse: String,
    opprettet: LocalDateTime,
    oppgaveType: String,
    tilstandString: String
) {
    private val tilstand = OppgaveTilstand.valueOf(tilstandString)
    val oppgave = Oppgave::class.primaryConstructor!!
        .apply { isAccessible = true }
        .call(OppgaveId(id, OppgaveType(oppgaveType), indeks), beskrivelse, opprettet, parseTilstand(tilstand))

    private fun parseTilstand(tilstand: OppgaveTilstand) = when (tilstand) {
        OppgaveTilstand.Uferdig -> Oppgave.Uferdig
        OppgaveTilstand.Ferdig -> Oppgave.Ferdig
    }
}
