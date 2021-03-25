package no.nav.dagpenger.innsyn.tjenester

import mu.KotlinLogging
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.dagpenger.innsyn.melding.Ettersendingsmelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal class EttersendingMottak(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandKey("naturligIdent") }
            validate { it.demandKey("journalpostId") }
            validate { it.demandKey("søknadsdata.behandlingskjedeId") }
            validate { it.requireValue("henvendelsestype", "ETTERSENDELSE") }
            validate { it.interestedIn("søknadsdata.vedlegg") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fnr = packet["naturligIdent"].asText()
        val søknadId = packet["søknadsdata.behandlingskjedeId"].asText()

        sikkerlogg.info { "Mottok ny søknad ($søknadId) for person ($fnr)." }

        Ettersendingsmelding(packet).also {
            personMediator.håndter(it.ettersending, it)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logg.error { problems }
    }
}
