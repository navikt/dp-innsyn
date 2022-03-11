package no.nav.dagpenger.innsyn.tjenester.paabegynte

import no.nav.dagpenger.innsyn.objectmother.ExternalPåbegyntObjectMother
import no.nav.dagpenger.innsyn.tjenester.paabegynt.toInternal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PåbegynteTransformeringTest {

    @Test
    fun `skal kun hente påbegynte for dagpenger og oversatte kodeverk`() {
        val externalEttersendelser = ExternalPåbegyntObjectMother.giveMePåbegynteForDAGOgBIL()

        val dagpengeEttersendelser = externalEttersendelser.toInternal()

        assertEquals(1, dagpengeEttersendelser.size)
        assertEquals(externalEttersendelser[1].behandlingsId, dagpengeEttersendelser[0].behandlingsId)
        assertEquals(externalEttersendelser[1].sistEndret, dagpengeEttersendelser[0].sistEndret)
        assertEquals("Søknad om dagpenger (ikke permittert)", dagpengeEttersendelser[0].tittel)
    }

    @Test
    fun `Skal kaste feil dersom tittel ikke er en dagpengekode`() {
        val bilPåbegynt = ExternalPåbegyntObjectMother.giveMeBILPåbegynt()

        assertThrows<IllegalArgumentException> { bilPåbegynt.toInternal() }
    }
}
