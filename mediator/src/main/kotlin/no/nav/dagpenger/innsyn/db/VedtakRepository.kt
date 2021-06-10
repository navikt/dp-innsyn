package no.nav.dagpenger.innsyn.db

import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import java.time.LocalDate

interface VedtakRepository {
    fun hentVedtakFor(
        fnr: String,
        fomFraDato: LocalDate? = null,
        tomFraDato: LocalDate? = null,
        fomTilDato: LocalDate? = null,
        tomTilDato: LocalDate? = null,
        status: List<Vedtak.Status> = emptyList(),
        offset: Int = 0,
        limit: Int = 20
    ): List<Vedtak>

    fun hentVedtakFor(fnr: String): List<Vedtak>
}
