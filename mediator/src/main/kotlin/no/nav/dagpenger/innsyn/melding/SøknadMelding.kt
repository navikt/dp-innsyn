package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.helse.rapids_rivers.JsonMessage

internal abstract class SøknadMelding(packet: JsonMessage) : Innsendingsmelding(packet) {
    abstract val søknadId: String?
    abstract val søknad: Søknad

    protected val søknadsType = Søknad.SøknadsType.valueOf(packet["type"].asText())
}
