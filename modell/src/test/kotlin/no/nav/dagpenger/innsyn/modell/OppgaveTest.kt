package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.VedleggOppgave
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OppgaveTest {

    @Test
    fun `En uferdig oppgave har status Uferdig`() {

        val oppgave = VedleggOppgave("1", "navn")
        assertEquals("Uferdig", oppgave.status)

    }
}

