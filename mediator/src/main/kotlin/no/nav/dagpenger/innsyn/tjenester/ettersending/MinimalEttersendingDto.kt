package no.nav.dagpenger.innsyn.tjenester.ettersending

import java.time.ZonedDateTime

data class MinimalEttersendingDto(
    val søknadId: String,
    val innsendtDato: ZonedDateTime?,
    val tittel: String
)
