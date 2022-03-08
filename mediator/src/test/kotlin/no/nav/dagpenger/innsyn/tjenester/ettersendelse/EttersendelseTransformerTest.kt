package no.nav.dagpenger.innsyn.tjenester.ettersendelse

import no.nav.dagpenger.innsyn.objectmother.ExternalEttersendelseObjectMother
import no.nav.dagpenger.innsyn.tjenester.ettersending.toInternal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class EttersendelseTransformerTest {

    @Test
    fun `skal kunne hente kun ettersendelser for dagpenger og ha oversatt kodeverk`() {
        val externalEttersendelser = ExternalEttersendelseObjectMother.giveMeEttersendelserForDAGOgBIL()

        val dagpengeEttersendelser = externalEttersendelser.toInternal()

        assertEquals(1, dagpengeEttersendelser.size)
        assertEquals(externalEttersendelser[1].behandlingsId, dagpengeEttersendelser[0].søknadId)
        assertEquals(externalEttersendelser[1].innsendtDato, dagpengeEttersendelser[0].innsendtDato)
        assertEquals("Søknad om dagpenger (ikke permittert)", dagpengeEttersendelser[0].tittel)
    }

    @Test
    fun `Skal kaste feil dersom tittel ikke er en dagpengekode`() {
        val bilEttersendelse = ExternalEttersendelseObjectMother.giveMeBILEttersendelse()

        assertThrows<IllegalArgumentException> { bilEttersendelse.toInternal() }
    }
}
