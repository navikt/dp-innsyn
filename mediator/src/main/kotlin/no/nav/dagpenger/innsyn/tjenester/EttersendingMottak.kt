package no.nav.dagpenger.innsyn.tjenester

import mu.KotlinLogging
import no.nav.dagpenger.innsyn.db.PersonRepository
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
    private val personRepository: PersonRepository
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandKey("søknadsdata") }
            validate { it.demandKey("søknadsdata.brukerBehandlingId") }
            validate { it.demandKey("naturligIdent") }
            validate { it.requireKey("søknadsdata.behandlingskjedeId") }
            validate { it.forbid("søknadsdata.behandlingskjedeId") }
            validate {
                it.interestedIn(
                    "søknadsdata.skjemaNummer",
                    "søknadsdata.vedlegg",
                    "søknadsdata.behandlingskjedeId"
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fnr = packet["naturligIdent"].asText()
        val søknadId = packet["søknadsdata.brukerBehandlingId"].asText()

        sikkerlogg.info { "Mottok ny søknad ($søknadId) for person ($fnr)." }

        Ettersendingsmelding(packet).also {
            personRepository.person(fnr).håndter(it.ettersending)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logg.error { problems }
    }
}
