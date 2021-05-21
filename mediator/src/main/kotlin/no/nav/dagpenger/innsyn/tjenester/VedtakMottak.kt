package no.nav.dagpenger.innsyn.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.dagpenger.innsyn.melding.Vedtaksmelding
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall.VedtakMottak")

internal class VedtakMottak(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("table", "SIAMO.VEDTAK") }
            validate {
                it.requireKey(
                    "op_ts",
                    "after.VEDTAK_ID",
                    "tokens.FODSELSNR",
                    "after.SAK_ID",
                )
            }
            validate { it.requireAny("after.VEDTAKTYPEKODE", listOf("O", "G")) }
            validate { it.requireAny("after.UTFALLKODE", listOf("JA", "NEI")) }
            validate { it.interestedIn("after", "tokens") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val fnr = packet["tokens"]["FODSELSNR"].asText()
        val vedtakId = packet["after"]["VEDTAK_ID"].asText()
        val sakId = packet["after"]["SAK_ID"].asText()

        withLoggingContext(
            "fagsakId" to sakId,
            "vedtakId" to vedtakId
        ) {
            logg.info { "Mottok nytt vedtak" }
            sikkerlogg.info { "Mottok nytt vedtak for person $fnr: ${packet.toJson()}" }

            Vedtaksmelding(packet).also {
                personMediator.håndter(it.vedtak, it)
            }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logg.error { problems }
        sikkerlogg.error { problems.toExtendedReport() }
    }
}
