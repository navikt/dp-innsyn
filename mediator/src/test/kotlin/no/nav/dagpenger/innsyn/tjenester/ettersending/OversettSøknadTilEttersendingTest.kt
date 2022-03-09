package no.nav.dagpenger.innsyn.tjenester.ettersending

import no.nav.dagpenger.innsyn.objectmother.SøknadObjectMother.giveMeListOfDigitalOgPapirSøknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OversettSøknadTilEttersendingTest {

    @Test
    fun `skal konvertere fra Søknad til MinimalEttersendelseDto`() {
        val søknader = giveMeListOfDigitalOgPapirSøknad()
        val visitor = OversettSøknadTilEttersending(søknader)
        val resultat = visitor.resultat()

        assertEquals(1, resultat.size)
        assertEquals("456", resultat[0].søknadId)
    }
}
