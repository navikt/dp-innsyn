package no.nav.dagpenger.innsyn

import mu.KotlinLogging
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.melding.Søknadsmelding
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
            validate { it.forbid("behandlingskjedeId") }
            validate { it.interestedIn("skjemaNummer", "vedlegg", "behandlingskjedeId") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fnr = packet["aktoerId"].asText()
        val søknadId = packet["brukerBehandlingId"].asText()

        sikkerlogg.info { "Mottok ny søknad ($søknadId) for person ($fnr)." }

        Søknadsmelding(packet).also {
            personRepository.person(fnr).håndter(it.søknad)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logg.error { problems }
    }
}
