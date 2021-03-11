package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.Søknad.Companion.erInnsendt
import no.nav.dagpenger.innsyn.modell.Søknad.Companion.erKomplett
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SøknadTest {
    @Test
    fun `søknad begynner i tilstanden innsendt`() {
        Søknad("id").also {
            assertTrue { erInnsendt(it) }
        }
    }

    @Test
    fun `søknad skal kunne mangle vedlegg`() {
        Søknad("id", listOf(Vedlegg("id"))).also {
            assertFalse { erKomplett(it) }
        }
    }
}
