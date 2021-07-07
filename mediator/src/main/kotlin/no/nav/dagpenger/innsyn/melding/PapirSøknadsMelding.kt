package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Kanal.Papir
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.time.LocalDateTime

internal class PapirSøknadsMelding(private val packet: JsonMessage) : Hendelsemelding(packet) {
    override val fødselsnummer get() = packet["fødselsnummer"].asText()
    private val journalpostId = packet["journalpostId"].asText()
    private val søknadsType = Søknad.SøknadsType.valueOf(packet["type"].asText())
    private val datoRegistrert: LocalDateTime = packet["datoRegistrert"].asLocalDateTime()

    internal val papirSøknad
        get() = Søknad(
            søknadId = null,
            journalpostId = journalpostId,
            skjemaKode = null,
            søknadsType = søknadsType,
            kanal = Papir,
            datoInnsendt = datoRegistrert,
            vedlegg = emptyList()
        )
}
