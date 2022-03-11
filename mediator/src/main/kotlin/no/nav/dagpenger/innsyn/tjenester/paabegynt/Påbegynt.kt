package no.nav.dagpenger.innsyn.tjenester.paabegynt

import java.time.ZonedDateTime

data class Påbegynt(
    val tittel: String,
    val behandlingsId: String,
    val sistEndret: ZonedDateTime
)
