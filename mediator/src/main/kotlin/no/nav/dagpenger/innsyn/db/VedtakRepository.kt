package no.nav.dagpenger.innsyn.db

import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import java.time.LocalDate

interface VedtakRepository {
    fun hentVedtakFor(
        fnr: String,
        fattetFom: LocalDate? = null,
        fattetTom: LocalDate? = null,
        status: List<Vedtak.Status> = emptyList(),
        offset: Int = 0,
        limit: Int = 20
    ): List<Vedtak>

    fun hentVedtakFor(fnr: String): List<Vedtak>
}
