package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.helse.rapids_rivers.JsonMessage

internal class Søknadsmelding(packet: JsonMessage) : Innsendingsmelding(packet) {
    private val søknadId = packet["søknadsData.brukerBehandlingId"].asText()
    private val skjemaNummer = packet["søknadsdata.skjemaNummer"].asText()
    private val søknadsType = Søknad.SøknadsType.valueOf(packet["type"].asText())
    internal val søknad
        get() = Søknad(
            søknadId,
            journalpostId,
            skjemaNummer,
            søknadsType,
            Kanal.Digital

        )
}
