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

internal class EttersendingSpleiserTest {

    @Test
    fun `Skal slå sammen ettersendelser, fjerne de uten dato, de som er for gamle og duplikater, samt sortere resultatet`() {
        val henvendelseOppslag = mockk<HenvendelseOppslag>()
        val ettersendelserFraHenvendelse = listOf(
            MinimalEttersendingDtoObjectMother.giveMeEttersending("123"),
            MinimalEttersendingDtoObjectMother.giveMeForGammelEttersending(),
            MinimalEttersendingDtoObjectMother.giveMeEttersendingUtenInnsendtDato(),
        )
        coEvery { henvendelseOppslag.hentEttersendelser(any()) } returns ettersendelserFraHenvendelse

        val personRepository = mockk<PersonRepository>()
        val nyeSøknaderFraDatabasen = listOf(SøknadObjectMother.giveDigitalSøknad(), SøknadObjectMother.giveDigitalSøknad())
        every { personRepository.hentSøknaderFor(fnr = any(), fom = null, tom = null) } returns nyeSøknaderFraDatabasen

        val ettersendingSpleiser = EttersendingSpleiser(henvendelseOppslag, personRepository)

        val alleEttersendelser = runBlocking {
            ettersendingSpleiser.hentEttersendelser("999")
        }

        assertEquals(1, alleEttersendelser.results().size)
        assertEquals("456", alleEttersendelser.results()[0].søknadId)
    }

    @Test
    fun `Hvis begge kilder gir ettersending med samme id, så skal den som har dato satt velges`() {
        val forventetKolliderendeSøknadId = "456"
        val henvendelseOppslag = mockk<HenvendelseOppslag>()
        val ettersendelserFraHenvendelse = listOf(
            MinimalEttersendingDtoObjectMother.giveMeEttersendingUtenInnsendtDato(forventetKolliderendeSøknadId),
        )
        coEvery { henvendelseOppslag.hentEttersendelser(any()) } returns ettersendelserFraHenvendelse

        val personRepository = mockk<PersonRepository>()
        val nyeSøknaderFraDatabasen = listOf(SøknadObjectMother.giveDigitalSøknad(forventetKolliderendeSøknadId))
        every { personRepository.hentSøknaderFor(fnr = any(), fom = null, tom = null) } returns nyeSøknaderFraDatabasen

        val ettersendingSpleiser = EttersendingSpleiser(henvendelseOppslag, personRepository)

        val alleEttersendelser = runBlocking {
            ettersendingSpleiser.hentEttersendelser("999")
        }

        assertEquals(1, alleEttersendelser.results().size)
        assertEquals(forventetKolliderendeSøknadId, alleEttersendelser.results()[0].søknadId)
        assertNotNull(alleEttersendelser.results()[0].datoInnsendt)
    }

    @Test
    fun `skal takle at henvendelse feiler og returnere resultatet fra databasen`() {
        val henvendelseOppslag = mockk<HenvendelseOppslag>()
        coEvery { henvendelseOppslag.hentEttersendelser(any()) } throws RuntimeException("Simulert feil i en test")

        val personRepository = mockk<PersonRepository>()
        val søknaderFraDatabasen = listOf(SøknadObjectMother.giveDigitalSøknad("456"))
        every { personRepository.hentSøknaderFor(fnr = any(), fom = null, tom = null) } returns søknaderFraDatabasen

        val ettersendingSpleiser = EttersendingSpleiser(henvendelseOppslag, personRepository)

        val alleEttersendelser = runBlocking {
            ettersendingSpleiser.hentEttersendelser("999")
        }

        assertEquals(1, alleEttersendelser.results().size)
        assertNotNull(alleEttersendelser.results().first { it.søknadId == "456" })
        // TODO: assertTrue(alleEttersendelser.failedSources().contains(KildeType.HENVENDELSE))
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

        assertEquals(0, alleEttersendelser.results().size)
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
        // TODO assertTrue(alleEttersendelser.failedSources().contains(KildeType.HENVENDELSE))
        assertTrue(alleEttersendelser.failedSources().contains(KildeType.DB))
    }
}
