package no.nav.dagpenger.innsyn

import mu.KotlinLogging
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.modell.Søknad
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal class Søknadsmottak(
    rapidsConnection: RapidsConnection,
    private val personRepository: PersonRepository
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.forbid("@id") }
            validate { it.requireKey("aktoerId", "brukerBehandlingId", "journalpostId") }
            validate { it.interestedIn("skjemaNummer") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fnr = packet["aktoerId"].asText()
        val søknadId = packet["brukerBehandlingId"].asText()
        val skjema = packet["skjemaNummer"].asText()

        sikkerlogg.info { "Mottok ny henvendelse ($søknadId) for person. Skjema ($skjema)" }

        personRepository.person(fnr).håndter(Søknad(søknadId))
    }
}
