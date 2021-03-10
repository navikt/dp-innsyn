package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.Søknad.Tilstand.Innsendt
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SøknadTest {
    @Test
    fun `søknad kan være innsendt`() {
        Søknad("id").also {
            assertTrue(it.tilstand is Innsendt)
        }
    }
}
