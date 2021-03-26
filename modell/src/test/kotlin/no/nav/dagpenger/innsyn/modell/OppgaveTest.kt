package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand.Uferdig
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OppgaveTest {
    @Test
    fun `En uferdig oppgave har status Uferdig`() {
        val oppgave = testOppgave.ny("1")
        val oppgaver = mutableSetOf<Oppgave>()

        oppgave.leggTilHvis(testOppgave, Uferdig, oppgaver)
        assertEquals(1, oppgaver.size)
    }

    private val testOppgave = OppgaveType("testOppgave")
}
