package no.nav.dagpenger.innsyn.tjenester.ettersending

import no.nav.dagpenger.innsyn.objectmother.SøknadObjectMother.giveDigitalSøknad
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

    @Test
    fun `skal konvertere søknad uten tittel og skjemakode`() {
        val søknader = giveDigitalSøknad(skjemakode = null, tittel = null)
        val visitor = OversettSøknadTilEttersending(listOf(søknader))
        val resultat = visitor.resultat()
        assertEquals(1, resultat.size)
        assertEquals("Uten tittel", resultat[0].tittel)
    }

    @Test
    fun `skal konvertere søknad uten tittel men med skjemakode`() {
        val søknader = giveDigitalSøknad(skjemakode = "NAV 04-16.04", tittel = null)
        val visitor = OversettSøknadTilEttersending(listOf(søknader))
        val resultat = visitor.resultat()
        assertEquals(1, resultat.size)
        assertEquals("Søknad om gjenopptak av dagpenger ved permittering", resultat[0].tittel)
    }
}
