package no.nav.dagpenger.innsyn.tjenester.ettersending

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.tjenester.HenvendelseOppslag
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZonedDateTime

internal class EttersendingMergererTest {

    @Test
    fun `Skal kunne slå sammen ettersendinger fra databasen og fra henvendelse`() {
        val henvendelseOppslag = mockk<HenvendelseOppslag>()
        val fraHenvendelse = createEttersendingFraHenvendelse()
        coEvery { henvendelseOppslag.hentEttersendelser(any()) } returns fraHenvendelse

        val personRepository = mockk<PersonRepository>()
        val fraDatabasen = createSøknadFraDatabasen()
        every { personRepository.hentSøknaderFor(any()) } returns fraDatabasen

        val ettersendingMergerer = EttersendingMergerer(henvendelseOppslag, personRepository)

        val alleEttersendinger = runBlocking {
            ettersendingMergerer.hentEttersendinger("123")
        }

        // assertEquals(2, alleEttersendinger.size)
        // assertNotNull(alleEttersendinger.first { it.søknadId == "123" })
        // assertNotNull(alleEttersendinger.first { it.søknadId == "456" })
    }

    // Fjerne duplikater
    // Filtrere bort papirsøknader
    // Sortering?

    private fun createEttersendingFraHenvendelse() =
        listOf(MinimalEttersendingDto("123", ZonedDateTime.now(), "Fra henvendelse"))

    private fun createSøknadFraDatabasen() = listOf(
        Søknad(
            "456",
            "j123",
            "kode",
            Søknad.SøknadsType.NySøknad,
            Kanal.Digital,
            LocalDateTime.now(),
            emptyList(),
            "Fra databasen"
        )
    )
}
