package no.nav.dagpenger.innsyn.tjenester.ettersending

import mu.KotlinLogging
import no.nav.dagpenger.innsyn.common.KildeType
import no.nav.dagpenger.innsyn.common.MultiSourceResult
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.tjenester.HenvendelseOppslag
import java.time.ZonedDateTime

internal class EttersendingSpleiser(
    private val henvendelseOppslag: HenvendelseOppslag,
    private val personRepository: PersonRepository
) {
    private val log = KotlinLogging.logger {}

    private val visningsgrenseForEttersendingAngittIÅr = 3L

    suspend fun hentEttersendelser(fnr: String): MultiSourceResult<MinimalEttersendingDto, KildeType> {
        val fraDatabasen = hentFraDB(fnr)
        val fraHenvendelse = hentFraHenvendelse(fnr)
        val alle = fraHenvendelse + fraDatabasen
        val unikeSisteTreÅr = alle.results()
            .filter { it.innsendtDatoMåVæreSatt() }
            .toSet()
            .filter { it.erNyereEnnTreÅr() }
            .sortedByDescending { it.datoInnsendt }

        return lagNyttResultatMedSammeKilderOgEventuelleFeiledeKilder(unikeSisteTreÅr, alle)
    }

    private fun hentFraDB(fnr: String) = try {
        val søknader = personRepository.hentSøknaderFor(
            fnr,
            fom = null,
            tom = null
        )
        val ettersendelser = søknader.toMinimalEttersending()
        MultiSourceResult.createSuccessfulResult(ettersendelser, KildeType.DB)
    } catch (e: Exception) {
        log.warn("Klarte ikke å hente data fra databasen: $e", e)
        MultiSourceResult.createErrorResult(KildeType.DB)
    }

    private fun List<Søknad>.toMinimalEttersending() = OversettSøknadTilEttersending(this).resultat()

    private suspend fun hentFraHenvendelse(fnr: String): MultiSourceResult<MinimalEttersendingDto, KildeType> = try {
        val ettersendelser = henvendelseOppslag.hentEttersendelser(fnr)
        MultiSourceResult.createSuccessfulResult(ettersendelser, KildeType.HENVENDELSE)
    } catch (e: Exception) {
        log.warn("Klarte ikke å hente data fra henvendelse: $e", e)
        MultiSourceResult.createErrorResult(KildeType.HENVENDELSE)
    }

    private fun MinimalEttersendingDto.innsendtDatoMåVæreSatt(): Boolean =
        if (datoInnsendt == null) {
            log.warn("Ettersendingen med id=$søknadId mangler innsendingsdato, den vil derfor ikke bli vist til sluttbruker.")
            false
        } else {
            true
        }

    private fun MinimalEttersendingDto.erNyereEnnTreÅr(): Boolean {
        val grenseForHvaSomSkalVises = ZonedDateTime.now().minusYears(visningsgrenseForEttersendingAngittIÅr)
        return datoInnsendt?.isAfter(grenseForHvaSomSkalVises) ?: false
    }

    private fun lagNyttResultatMedSammeKilderOgEventuelleFeiledeKilder(
        unikeEttersendelser: List<MinimalEttersendingDto>,
        alleEttersendelser: MultiSourceResult<MinimalEttersendingDto, KildeType>
    ) = MultiSourceResult(
        unikeEttersendelser,
        alleEttersendelser.successFullSources(),
        alleEttersendelser.failedSources()
    )
}
