package no.nav.dagpenger.innsyn

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val logger = KotlinLogging.logger { }

internal class StatusLøsningService(rapidsConnection: RapidsConnection) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf("Status")) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("fødselsnummer") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val fnr = packet["fødselsnummer"].asText()

        packet["@løsning"] = mapOf(
            "Status" to "$fnr har ingen status"
        )

        context.send(packet.toJson())
    }

    override fun onError(problems: MessageProblems, context: RapidsConnection.MessageContext) {
        logger.error { problems }
    }
}
