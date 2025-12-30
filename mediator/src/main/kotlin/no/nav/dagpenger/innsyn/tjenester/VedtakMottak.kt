package no.nav.dagpenger.innsyn.tjenester

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.isMissingOrNull
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import io.micrometer.core.instrument.MeterRegistry
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.dagpenger.innsyn.melding.Vedtaksmelding

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal class VedtakMottak(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                precondition { it.requireValue("table", "SIAMO.VEDTAK") }
                validate {
                    it.requireKey(
                        "op_ts",
                        "after.VEDTAK_ID",
                        "after.SAK_ID",
                        "after.FRA_DATO",
                    )
                    it.requireAny("after.VEDTAKTYPEKODE", listOf("O", "G"))
                    it.requireAny("after.UTFALLKODE", listOf("JA", "NEI"))
                    it.interestedIn("after", "tokens")
                    it.interestedIn("after.TIL_DATO")
                    it.interestedIn("tokens.FODSELSNR")
                    it.interestedIn("FODSELSNR")
                }
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
