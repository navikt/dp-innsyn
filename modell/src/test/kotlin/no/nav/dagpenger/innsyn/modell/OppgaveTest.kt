package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.OppgaveType.Companion.vedlegg
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OppgaveTest {

    @Test
    fun `En uferdig oppgave har status Uferdig`() {

        val oppgave = Oppgave("1", vedlegg)
        assertEquals("Uferdig", oppgave.status)
    }
}

