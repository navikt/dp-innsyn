package no.nav.dagpenger.innsyn.tjenester.ettersending

import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.tjenester.HenvendelseOppslag

internal class EttersendingSpleiser(
    val henvendelseOppslag: HenvendelseOppslag,
    val personRepository: PersonRepository
) {

    suspend fun hentEttersendinger(fnr: String): List<MinimalEttersendingDto> {
        val ettersendingerFraHenvendelse = henvendelseOppslag.hentEttersendelser(fnr)
        val søknaderFraDb = personRepository.hentSøknaderFor(fnr)
        val ettersendingerFraDb = søknaderFraDb.toMinimalEttersending()
        val alleEttersendinger = ettersendingerFraHenvendelse + ettersendingerFraDb

        val unikeEttersendinger = alleEttersendinger
            .toSet()
            .sortedByDescending { ettersending ->
                ettersending.innsendtDato
            }

        return unikeEttersendinger
    }
}

internal fun List<Søknad>.toMinimalEttersending() =
    SøknadTilMinimalEttersendelseTransformer(this).resultat()
