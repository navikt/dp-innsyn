package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.OppgaveType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OppgaveTest {

    @Test
    fun `En uferdig oppgave har status Uferdig`() {

        val oppgave = Oppgave("1", testOppgave)
        assertEquals("Uferdig", oppgave.status.name)
    }

    val testOppgave = OppgaveType("testOppgave")
}
