package no.nav.dagpenger.innsyn

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.innsyn.meldinger.ØnskerStatusMelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

typealias Fødselsnummer = String

private val logger = KotlinLogging.logger { }

internal class Mediator(private val rapidsConnection: RapidsConnection) : River.PacketListener {
    private val observers = mutableMapOf<UUID, StatusObserver>()

    init {
        River(rapidsConnection).apply {
            validate { it.demandKey("@behov") }
            validate { it.requireKey("session") }
            validate { it.requireKey("@løsning") }
            validate { it.requireKey("fødselsnummer") }
        }.register(this)
    }

    fun ønskerStatus(fødselsnummer: Fødselsnummer, observer: StatusObserver) =
        observers.put(observer.uuid, observer).also {
            ønskerStatus(observer.uuid, fødselsnummer)
        }

    private fun ønskerStatus(uuid: UUID, fødselsnummer: String) {
        rapidsConnection.publish(ØnskerStatusMelding(uuid, fødselsnummer).toJson())
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext): Unit = runBlocking {
        val sessionId = UUID.fromString(packet["session"].asText())
        val behov = packet["@behov"].joinToString { it.asText() }

        withLoggingContext(
            "sessionId" to sessionId.toString(),
            "behov" to behov
        ) {
            logger.info { "Har fått løsning på pakke" }
            observers.filterKeys { it == sessionId }.values.onEach {
                logger.info { "Svarer på riktig session" }
                it.statusOppdatert(packet.toJson())
            }.ifEmpty { logger.info { "Fant ikke noen session som venter på svar" } }
        }
    }
}

internal interface StatusObserver {
    val uuid: UUID

    suspend fun statusOppdatert(status: String)
}
