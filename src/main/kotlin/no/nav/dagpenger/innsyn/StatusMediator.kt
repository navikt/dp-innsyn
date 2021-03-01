package no.nav.dagpenger.innsyn

import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.innsyn.meldinger.ØnskerStatusMelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

typealias Fødselsnummer = String

internal class StatusMediator(private val rapidsConnection: RapidsConnection) : River.PacketListener {
    private val observers = mutableMapOf<Fødselsnummer, StatusObserver>()

    init {
        River(rapidsConnection).apply {
            validate { it.demandKey("@behov") }
            validate { it.requireKey("@løsning") }
            validate { it.requireKey("fødselsnummer") }
        }.register(this)
    }

    fun ønskerStatus(fødselsnummer: Fødselsnummer, observer: StatusObserver) = observers.put(fødselsnummer, observer).also {
        ønskerStatus(fødselsnummer)
    }

    private fun ønskerStatus(fødselsnummer: String) {
        rapidsConnection.publish(ØnskerStatusMelding(fødselsnummer).toJson())
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) =
        runBlocking {
            val fødselsnummer = packet["fødselsnummer"].asText()
            observers.filterKeys { it == fødselsnummer }.values.forEach { it.statusOppdatert(packet.toJson()) }
        }
}

internal interface StatusObserver {
    suspend fun statusOppdatert(status: String)
}
