package no.nav.dagpenger.innsyn.tjenester

import mu.withLoggingContext
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.dagpenger.innsyn.melding.PapirSøknadsMelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class PapirSøknadMottak(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "innsending_mottatt") }
            validate { it.demandKey("fødselsnummer") }
            validate { it.demandKey("journalpostId") }
            validate { it.requireKey("datoRegistrert") }
            validate { it.requireAny("type", listOf("NySøknad", "Gjenopptak")) }
            validate { it.forbid("søknadsData.brukerBehandlingId") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val journalpostId = packet["journalpostId"].asText()

        withLoggingContext(
            "journalpostId" to journalpostId
        ) {
            PapirSøknadsMelding(packet).also {
                personMediator.håndter(it.papirSøknad, it)
            }
        }
    }
}
