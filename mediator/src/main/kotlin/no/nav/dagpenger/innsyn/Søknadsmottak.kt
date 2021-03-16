package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.modell.Søknad
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class Søknadsmottak(rapidsConnection: RapidsConnection, private val personRepository: PersonRepository) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("søknadId") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fnr = packet["fødselsnummer"].asText()
        val søknadId = packet["søknadId"].asText()

        personRepository.person(fnr).håndter(Søknad(søknadId))
    }
}
