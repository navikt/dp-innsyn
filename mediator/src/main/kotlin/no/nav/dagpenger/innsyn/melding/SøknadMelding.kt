package no.nav.dagpenger.innsyn.melding

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad

internal abstract class SøknadMelding(
    packet: JsonMessage,
) : Innsendingsmelding(packet) {
    abstract val søknadId: String?
    abstract val søknad: Søknad

    protected val søknadsType = Søknad.SøknadsType.valueOf(packet["type"].asText())
}
