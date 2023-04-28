package no.nav.dagpenger.innsyn.tjenester.ettersending

import java.time.ZonedDateTime

data class MinimalEttersendingDto(
    val søknadId: String,
    val datoInnsendt: ZonedDateTime?,
    val tittel: String,
) {
    override fun equals(other: Any?) = other is MinimalEttersendingDto && this.søknadId == other.søknadId

    override fun hashCode() = søknadId.hashCode()
}
