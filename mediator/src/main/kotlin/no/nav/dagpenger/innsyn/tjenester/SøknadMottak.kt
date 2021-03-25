package no.nav.dagpenger.innsyn.tjenester

import mu.KotlinLogging
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.dagpenger.innsyn.melding.Søknadsmelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal class SøknadMottak(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.interestedIn("brukerBehandlingId") }
            validate { it.interestedIn("aktoerId") }
            validate { it.demandKey("journalpostId") }
            validate { it.forbid("behandlingskjedeId") }
            validate { it.interestedIn("skjemaNummer", "vedlegg", "behandlingskjedeId") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info { packet.toJson() }

        val fnr = packet["aktoerId"].asText()
        val søknadId = packet["brukerBehandlingId"].asText()

        sikkerlogg.info { "Mottok ny søknad ($søknadId) for person ($fnr)." }
        sikkerlogg.info { packet.toJson() }

        Søknadsmelding(packet).also {
            personMediator.håndter(it.søknad, it)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logg.error { problems }
    }
}
