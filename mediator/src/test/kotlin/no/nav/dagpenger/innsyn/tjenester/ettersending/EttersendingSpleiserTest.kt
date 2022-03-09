package no.nav.dagpenger.innsyn.tjenester.ettersending

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.innsyn.common.KildeType
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.objectmother.MinimalEttersendingDtoObjectMother
import no.nav.dagpenger.innsyn.objectmother.SøknadObjectMother
import no.nav.dagpenger.innsyn.tjenester.HenvendelseOppslag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class EttersendingSpleiserTest {

    @Test
    fun `Skal kunne slå sammen ettersendelser fra databasen og fra henvendelse, samt fjerne duplikater og sortere`() {
        val henvendelseOppslag = mockk<HenvendelseOppslag>()
        val gammelEttersendingFraHenvendelse = MinimalEttersendingDtoObjectMother.giveMeEttersending(søknadId = "123", innsendtDato = ZonedDateTime.now().minusYears(2))
        coEvery { henvendelseOppslag.hentEttersendelser(any()) } returns listOf(gammelEttersendingFraHenvendelse)

        val personRepository = mockk<PersonRepository>()
        val nyeSøknaderFraDatabasen = listOf(SøknadObjectMother.giveDigitalSøknad(), SøknadObjectMother.giveDigitalSøknad())
        every { personRepository.hentSøknaderFor(fnr = any(), fom = null, tom = null) } returns nyeSøknaderFraDatabasen

        val ettersendingSpleiser = EttersendingSpleiser(henvendelseOppslag, personRepository)

        val alleEttersendelser = runBlocking {
            ettersendingSpleiser.hentEttersendelser("999")
        }

        assertEquals(2, alleEttersendelser.results().size)
        assertEquals("456", alleEttersendelser.results()[0].søknadId)
        assertEquals("123", alleEttersendelser.results()[1].søknadId)
    }

    @Test
    fun `skal takle at henvendelse feiler og returnere resultatet fra databasen`() {
        val henvendelseOppslag = mockk<HenvendelseOppslag>()
        coEvery { henvendelseOppslag.hentEttersendelser(any()) } throws RuntimeException("Simulert feil i en test")

        val personRepository = mockk<PersonRepository>()
        val søknaderFraDatabasen = listOf(SøknadObjectMother.giveDigitalSøknad())
        every { personRepository.hentSøknaderFor(fnr = any(), fom = null, tom = null) } returns søknaderFraDatabasen

        val ettersendingSpleiser = EttersendingSpleiser(henvendelseOppslag, personRepository)

        val alleEttersendelser = runBlocking {
            ettersendingSpleiser.hentEttersendelser("999")
        }

        assertEquals(1, alleEttersendelser.results().size)
        assertNotNull(alleEttersendelser.results().first { it.søknadId == "456" })
        assertTrue(alleEttersendelser.failedSources().contains(KildeType.HENVENDELSE))
    }

    @Test
    fun `skal takle at databasen feiler og returnere resultatet fra henvendelse`() {
        val henvendelseOppslag = mockk<HenvendelseOppslag>()
        val fraHenvendelse = MinimalEttersendingDtoObjectMother.giveMeEttersending(søknadId = "678")
        coEvery { henvendelseOppslag.hentEttersendelser(any()) } returns listOf(fraHenvendelse)

        val personRepository = mockk<PersonRepository>()
        every { personRepository.hentSøknaderFor(any()) } throws RuntimeException("Simulert feil i en test")

        val ettersendingSpleiser = EttersendingSpleiser(henvendelseOppslag, personRepository)

        val alleEttersendelser = runBlocking {
            ettersendingSpleiser.hentEttersendelser("999")
        }

        assertEquals(1, alleEttersendelser.results().size)
        assertNotNull(alleEttersendelser.results().first { it.søknadId == "678" })
        assertTrue(alleEttersendelser.failedSources().contains(KildeType.DB))
    }

    @Test
    fun `skal takle at både databasen og henvendelse feiler`() {
        val henvendelseOppslag = mockk<HenvendelseOppslag>()
        coEvery { henvendelseOppslag.hentEttersendelser(any()) } throws RuntimeException("Simulert feil i en test")

        val personRepository = mockk<PersonRepository>()
        every { personRepository.hentSøknaderFor(any()) } throws RuntimeException("Simulert feil i en test")

        val ettersendingSpleiser = EttersendingSpleiser(henvendelseOppslag, personRepository)

        val alleEttersendelser = runBlocking {
            ettersendingSpleiser.hentEttersendelser("999")
        }

        assertEquals(0, alleEttersendelser.results().size)
        assertTrue(alleEttersendelser.failedSources().contains(KildeType.HENVENDELSE))
        assertTrue(alleEttersendelser.failedSources().contains(KildeType.DB))
    }
}
