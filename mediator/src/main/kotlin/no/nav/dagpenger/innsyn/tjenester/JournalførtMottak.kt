package no.nav.dagpenger.innsyn.tjenester

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import io.micrometer.core.instrument.MeterRegistry
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.dagpenger.innsyn.melding.Journalførtmelding

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall.JournalførtMottak")

internal class JournalførtMottak(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                precondition {
                    it.requireValue("@event_name", "innsending_ferdigstilt")
                }
                validate {
                    it.requireKey("fagsakId")
                    it.requireKey("journalpostId")
                    it.requireKey("fødselsnummer")
                }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val fnr = packet["fødselsnummer"].asText()
        val journalpostId = packet["journalpostId"].asText()
        val fagsakId = packet["fagsakId"].asText()

        withLoggingContext(
            "journalpostId" to journalpostId,
            "fagsakId" to fagsakId,
        ) {
            logg.info { "Mottok ferdig journalføring." }
            sikkerlogg.info { "Mottok ferdig journalføring ($fnr): ${packet.toJson()}" }

            Journalførtmelding(packet).also {
                personMediator.håndter(it.journalføring, it)
            }
        }
    }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
        metadata: MessageMetadata,
    ) {
        logg.debug { problems }
    }
}
