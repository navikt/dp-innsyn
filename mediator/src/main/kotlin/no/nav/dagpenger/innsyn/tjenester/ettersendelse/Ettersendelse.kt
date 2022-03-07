package no.nav.dagpenger.innsyn.tjenester.ettersendelse

import java.time.ZonedDateTime

data class Ettersendelse(
    val søknadId: String,
    val innsendtDato: ZonedDateTime?,
    val tittel: String
)
