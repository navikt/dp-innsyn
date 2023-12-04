package no.nav.dagpenger.innsyn.tjenester.paabegynt

import no.nav.dagpenger.innsyn.tjenester.Lenker
import java.time.ZonedDateTime

data class Påbegynt(
    val tittel: String,
    @Deprecated("Bruk #søknadId i stedet")
    val behandlingsId: String,
    val søknadId: String,
    val sistEndret: ZonedDateTime,
    val erNySøknadsdialog: Boolean,
    val endreLenke: String =
        if (erNySøknadsdialog) {
            Lenker.påbegyntNySøknadsdialogIngress(
                søknadId,
            )
        } else {
            Lenker.påbegyntGammelSøknadsdialogIngress(søknadId)
        },
)
