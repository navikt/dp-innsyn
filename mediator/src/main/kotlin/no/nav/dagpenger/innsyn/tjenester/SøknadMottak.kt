package no.nav.dagpenger.innsyn.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
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
            validate { it.demandValue("@event_name", "innsending_mottatt") }
            validate { it.demandKey("fødselsnummer") }
            validate { it.demandKey("journalpostId") }
            validate { it.demandKey("søknadsData.brukerBehandlingId") }
            validate { it.requireAny("type", listOf("NySøknad", "Gjenopptak")) }
            validate { it.interestedIn("søknadsData.vedlegg") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fnr = packet["fødselsnummer"].asText()
        val søknadId = packet["søknadsData.brukerBehandlingId"].asText()
        val journalpostId = packet["journalpostId"].asText()

        withLoggingContext(
            "søknadId" to søknadId,
            "journalpostId" to journalpostId
        ) {
            logg.info { "Mottok ny søknad." }
            sikkerlogg.info { "Mottok ny søknad for person $fnr: ${packet.toJson()}" }

            Søknadsmelding(packet).also {
                personMediator.håndter(it.søknad, it)
            }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logg.error { problems }
    }
}
