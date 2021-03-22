package no.nav.dagpenger.innsyn.tjenester

import mu.KotlinLogging
import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.melding.Vedtaksmelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal class VedtakMottak(
    rapidsConnection: RapidsConnection,
    private val personRepository: PersonRepository
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandKey("vedtakId") }
            validate { it.demandKey("fødselsnummer") }
            validate { it.demandKey("søknadId") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fnr = packet["fødselsnummer"].asText()
        val søknadId = packet["søknadId"].asText()

        sikkerlogg.info { "Mottok nytt vedtak ($søknadId) for person ($fnr)." }

        Vedtaksmelding(packet).also {
            personRepository.person(fnr).håndter(it.vedtak)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logg.error { problems }
    }
}
