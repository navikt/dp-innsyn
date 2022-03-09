package no.nav.dagpenger.innsyn.tjenester.ettersending

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.objectmother.SøknadObjectMother
import no.nav.dagpenger.innsyn.tjenester.HenvendelseOppslag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class EttersendingSpleiserTest {

    @Test
    fun `Skal kunne slå sammen ettersendelser fra databasen og fra henvendelse`() {
        val henvendelseOppslag = mockk<HenvendelseOppslag>()
        val fraHenvendelse = createEttersendingFraHenvendelse()
        coEvery { henvendelseOppslag.hentEttersendelser(any()) } returns fraHenvendelse

        val personRepository = mockk<PersonRepository>()
        val søknaderFraDatabasen = listOf(SøknadObjectMother.giveDigitalSøknad(), SøknadObjectMother.giveDigitalSøknad())
        every { personRepository.hentSøknaderFor(any()) } returns søknaderFraDatabasen

        val ettersendingSpleiser = EttersendingSpleiser(henvendelseOppslag, personRepository)

        val alleEttersendelser = runBlocking {
            ettersendingSpleiser.hentEttersendelser("999")
        }

        assertEquals(2, alleEttersendelser.size)
        assertNotNull(alleEttersendelser.first { it.søknadId == "456" })
        assertNotNull(alleEttersendelser.first { it.søknadId == "678" })
        assertEquals("456", alleEttersendelser[0].søknadId)
        assertEquals("678", alleEttersendelser[1].søknadId)
    }

    private fun createEttersendingFraHenvendelse() =
        listOf(MinimalEttersendingDto("678", ZonedDateTime.now().minusYears(2), "Fra henvendelse"))
}
