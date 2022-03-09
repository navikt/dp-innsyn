package no.nav.dagpenger.innsyn.tjenester.ettersending

import mu.KotlinLogging
import no.nav.dagpenger.innsyn.common.KildeType
import no.nav.dagpenger.innsyn.common.MultiSourceResult
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.tjenester.HenvendelseOppslag

internal class EttersendingSpleiser(
    private val henvendelseOppslag: HenvendelseOppslag,
    private val personRepository: PersonRepository
) {
    private val log = KotlinLogging.logger {}

    suspend fun hentEttersendelser(fnr: String): MultiSourceResult<MinimalEttersendingDto, KildeType> {
        val ettersendelserFraDb = hentFraDB(fnr)
        val ettersendelserFraHenvendelse = hentFraHenvendelse(fnr)
        val alleEttersendelser = ettersendelserFraHenvendelse + ettersendelserFraDb

        return fjernDuplikaterOgSorter(alleEttersendelser)
    }

    private fun hentFraDB(fnr: String) = try {
        val søknader = personRepository.hentSøknaderFor(fnr)
        val ettersendelser = søknader.toMinimalEttersending()
        MultiSourceResult.createSuccessfulResult(ettersendelser, KildeType.DB)
    } catch (e: Exception) {
        log.warn("Klarte ikke å hente data fra databasen: $e", e)
        MultiSourceResult.createErrorResult(KildeType.DB)
    }

    private suspend fun hentFraHenvendelse(fnr: String) = try {
        val ettersendelser = henvendelseOppslag.hentEttersendelser(fnr)
        MultiSourceResult.createSuccessfulResult(ettersendelser, KildeType.HENVENDELSE)
    } catch (e: Exception) {
        log.warn("Klarte ikke å hente data fra henvendelse: $e", e)
        MultiSourceResult.createErrorResult(KildeType.HENVENDELSE)
    }

    private fun fjernDuplikaterOgSorter(alleEttersendelser: MultiSourceResult<MinimalEttersendingDto, KildeType>): MultiSourceResult<MinimalEttersendingDto, KildeType> {
        val unikeEttersendelser = alleEttersendelser
            .results()
            .toSet()
            .sortedByDescending { ettersending ->
                ettersending.innsendtDato
            }

        return MultiSourceResult(unikeEttersendelser, alleEttersendelser.successFullSources(), alleEttersendelser.failedSources())
    }
}

internal fun List<Søknad>.toMinimalEttersending() =
    OversettSøknadTilEttersending(this).resultat()
