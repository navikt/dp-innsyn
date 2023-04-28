package no.nav.dagpenger.innsyn.objectmother

import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import java.time.LocalDateTime

object SøknadObjectMother {
    fun giveMeListOfDigitalOgPapirSøknad() = listOf(
        giveDigitalSøknad(),
        giveMePapirSøknad(),
    )

    fun giveMePapirSøknad(søknadId: String = "123") = Søknad(
        søknadId,
        "journalpostId-123",
        "NAV123",
        Søknad.SøknadsType.NySøknad,
        Kanal.Papir,
        LocalDateTime.now(),
        emptyList(),
        "Papir søknad",
    )

    fun giveDigitalSøknad(søknadId: String = "456", skjemakode: String? = "NAV456", tittel: String? = "Digital søknad") = Søknad(
        søknadId,
        "journalpostId-456",
        skjemakode,
        Søknad.SøknadsType.NySøknad,
        Kanal.Digital,
        LocalDateTime.now(),
        emptyList(),
        tittel,
    )
}
