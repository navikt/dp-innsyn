package no.nav.dagpenger.innsyn.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.dagpenger.innsyn.melding.Journalførtmelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal class JournalførtMottak(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "innsending_ferdigstilt") }
            validate { it.requireKey("fagsakId") }
            validate { it.requireKey("journalpostId") }
            validate { it.requireKey("fødselsnummer") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fnr = packet["fødselsnummer"].asText()
        val journalpostId = packet["journalpostId"].asText()
        val fagsakId = packet["fagsakId"].asText()

        withLoggingContext(
            "journalpostId" to journalpostId,
            "fagsakId" to fagsakId
        ) {
            logg.info { "Mottok ferdig journalføring." }
            sikkerlogg.info { "Mottok ferdig journalføring ($fnr): ${packet.toJson()}" }

            Journalførtmelding(packet).also {
                personMediator.håndter(it.journalføring, it)
            }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logg.error { problems }
    }

    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        logg.error { error }
    }
}
