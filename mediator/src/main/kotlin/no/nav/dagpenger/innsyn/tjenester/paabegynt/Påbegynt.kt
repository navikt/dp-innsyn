package no.nav.dagpenger.innsyn.tjenester.paabegynt

import java.time.ZonedDateTime

data class Påbegynt(
    val tittel: String,
    @Deprecated("Bruk #søknadId i stedet")
    val behandlingsId: String,
    val søknadId: String,
    val sistEndret: ZonedDateTime
)
