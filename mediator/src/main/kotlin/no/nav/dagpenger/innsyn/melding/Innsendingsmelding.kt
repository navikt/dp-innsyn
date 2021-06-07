package no.nav.dagpenger.innsyn.melding

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.time.LocalDateTime

internal abstract class Innsendingsmelding(packet: JsonMessage) : Hendelsemelding(packet) {
    override val fødselsnummer = packet["fødselsnummer"].asText()
    internal val journalpostId: String = packet["journalpostId"].asText()
    internal val datoRegistrert: LocalDateTime = packet["datoRegistrert"].asLocalDateTime()
}