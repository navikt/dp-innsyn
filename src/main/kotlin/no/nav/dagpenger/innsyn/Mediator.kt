package no.nav.dagpenger.innsyn

import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.innsyn.meldinger.ØnskerStatusMelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

typealias Fødselsnummer = String

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

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) =
        runBlocking {
            val sessionId = UUID.fromString(packet["session"].asText())
            observers.filterKeys { it == sessionId }.values.forEach { it.statusOppdatert(packet.toJson()) }
        }
}

internal interface StatusObserver {
    val uuid: UUID

    suspend fun statusOppdatert(status: String)
}
