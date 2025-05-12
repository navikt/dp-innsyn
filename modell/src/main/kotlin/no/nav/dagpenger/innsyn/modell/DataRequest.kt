package no.nav.dagpenger.innsyn.modell

import java.time.LocalDate

data class DataRequest(
    val personIdent: String,
    val fraOgMedDato: LocalDate,
    val tilOgMedDato: LocalDate?,
)
