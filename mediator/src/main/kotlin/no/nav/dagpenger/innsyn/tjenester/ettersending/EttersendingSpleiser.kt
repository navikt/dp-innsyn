package no.nav.dagpenger.innsyn.tjenester.ettersending

import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.tjenester.HenvendelseOppslag

internal class EttersendingSpleiser(
    val henvendelseOppslag: HenvendelseOppslag,
    val personRepository: PersonRepository
) {

    suspend fun hentEttersendelser(fnr: String): List<MinimalEttersendingDto> {
        val ettersendelserFraHenvendelse = henvendelseOppslag.hentEttersendelser(fnr)
        val søknaderFraDb = personRepository.hentSøknaderFor(fnr)
        val ettersendelserFraDb = søknaderFraDb.toMinimalEttersending()
        val alleEttersendelser = ettersendelserFraHenvendelse + ettersendelserFraDb

        val unikeEttersendelser = alleEttersendelser
            .toSet()
            .sortedByDescending { ettersending ->
                ettersending.innsendtDato
            }

        return unikeEttersendelser
    }
}

internal fun List<Søknad>.toMinimalEttersending() =
    OversettSøknadTilEttersending(this).resultat()
