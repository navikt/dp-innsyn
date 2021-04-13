package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Hendelse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TidslinjeTest {

    @Test
    fun `skal kunne opprette hendelse på tidslinje`() {
        Tidslinje().apply {
            leggTil(TestHendelse())
        }.also {
            assertEquals(1, it.size)
        }
    }

    private class TestHendelse() : Hendelse("", emptySet())
}
