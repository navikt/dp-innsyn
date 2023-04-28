package no.nav.dagpenger.innsyn.db

import no.nav.dagpenger.innsyn.modell.hendelser.Innsending
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad.SøknadsType
import java.time.LocalDate

interface SøknadRepository {
    fun hentSøknaderFor(
        fnr: String,
        fom: LocalDate? = null,
        tom: LocalDate? = null,
        type: List<SøknadsType> = emptyList(),
        kanal: List<Kanal> = emptyList(),
        offset: Int = 0,
        limit: Int = 20,
    ): List<Søknad>

    fun hentSøknaderFor(fnr: String): List<Søknad>
    fun hentVedleggFor(søknadsId: String): List<Innsending.Vedlegg>
}
