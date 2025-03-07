package no.nav.dagpenger.innsyn.tjenester

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.isMissingOrNull
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.dagpenger.innsyn.melding.Vedtaksmelding

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall.VedtakMottak")

internal class VedtakMottak(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                validate { it.demandValue("table", "SIAMO.VEDTAK") }
                validate {
                    it.requireKey(
                        "op_ts",
                        "after.VEDTAK_ID",
                        "after.SAK_ID",
                        "after.FRA_DATO",
                    )
                }
                validate { it.requireAny("after.VEDTAKTYPEKODE", listOf("O", "G")) }
                validate { it.requireAny("after.UTFALLKODE", listOf("JA", "NEI")) }
                validate { it.interestedIn("after", "tokens") }
                validate { it.interestedIn("after.TIL_DATO") }
                validate { it.interestedIn("tokens.FODSELSNR") }
                validate { it.interestedIn("FODSELSNR") }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val fnr = packet.fødselsnummer()
        val vedtakId = packet["after"]["VEDTAK_ID"].asText()
        val sakId = packet["after"]["SAK_ID"].asText()

        withLoggingContext(
            "fagsakId" to sakId,
            "vedtakId" to vedtakId,
        ) {
            logg.info { "Mottok nytt vedtak" }
            sikkerlogg.info { "Mottok nytt vedtak for person $fnr: ${packet.toJson()}" }

            Vedtaksmelding(packet).also {
                personMediator.håndter(it.vedtak, it)
            }
        }
    }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
        metadata: MessageMetadata,
    ) {
        logg.debug { problems }
        sikkerlogg.debug { problems.toExtendedReport() }
    }
}

internal fun JsonMessage.fødselsnummer(): String =
    if (this["tokens"].isMissingOrNull()) this["FODSELSNR"].asText() else this["tokens"]["FODSELSNR"].asText()
