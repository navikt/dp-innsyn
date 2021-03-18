package no.nav.dagpenger.innsyn

import mu.KotlinLogging
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.modell.Søknad
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal class Søknadsmottak(
    rapidsConnection: RapidsConnection,
    private val personRepository: PersonRepository
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandKey("brukerBehandlingId") }
            validate { it.demandKey("aktoerId") }
            validate { it.demandKey("journalpostId") }
            validate { it.interestedIn("skjemaNummer", "vedlegg", "behandlingskjedeId") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fnr = packet["aktoerId"].asText()
        val søknadId = packet["brukerBehandlingId"].asText()
        val skjema = packet["skjemaNummer"].asText()
        val vedlegg = packet["vedlegg"].toPrettyString()

        sikkerlogg.info { "Mottok ny henvendelse ($søknadId) for person. Skjema ($skjema)" }
        sikkerlogg.info { "Vedleggslista ser sånn ut: $vedlegg" }

        personRepository.person(fnr).håndter(Søknad(søknadId))
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logg.error { problems }
    }
}
