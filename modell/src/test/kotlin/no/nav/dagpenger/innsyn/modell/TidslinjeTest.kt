package no.nav.dagpenger.innsyn.modell

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TidslinjeTest {
    @Test
    fun `en hendelse skal inn på tidslinjen`() {
        Tidslinje().also { tidslinje ->
            val hendelse = Søknad("id")
            tidslinje.leggTil(hendelse)

            assertTrue(tidslinje.all { it == hendelse })
        }
    }
}
