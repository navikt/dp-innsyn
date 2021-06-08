package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal.Papir
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.helse.rapids_rivers.JsonMessage

internal class PapirSøknadsMelding(private val packet: JsonMessage) : Hendelsemelding(packet) {

    private val journalpostId = packet["journalpostId"].asText()
    private val søknadsType = Søknad.SøknadsType.valueOf(packet["type"].asText())

    internal val papirSøknad
        get() = Søknad(
            null,
            journalpostId,
            null,
            søknadsType,
            Papir

        )
    override val fødselsnummer: String
        get() = packet["fødselsnummer"].asText()
}
