package no.nav.dagpenger.innsyn.tjenester

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import io.micrometer.core.instrument.MeterRegistry
import no.nav.dagpenger.innsyn.Metrikker
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.dagpenger.innsyn.melding.Ettersendingsmelding

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal class EttersendingMottak(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                precondition {
                    it.requireValue("@event_name", "innsending_mottatt")
                }
                validate {
                    it.requireKey(
                        "fødselsnummer",
                        "journalpostId",
                        "datoRegistrert",
                        "skjemaKode",
                        "søknadsData.brukerBehandlingId",
                        "søknadsData.behandlingskjedeId",
                        "tittel",
                    )
                    it.requireAny("type", listOf("Ettersending"))
                    it.interestedIn("søknadsData.vedlegg")
                }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val fnr = packet["fødselsnummer"].asText()
        val søknadId = packet["søknadsData.behandlingskjedeId"].asText()
        val journalpostId = packet["journalpostId"].asText()

        withLoggingContext(
            "søknadId" to søknadId,
            "journalpostId" to journalpostId,
        ) {
            logg.info { "Mottok ny ettersending." }
            sikkerlogg.info { "Mottok ny ettersending for person $fnr: ${packet.toJson()}" }

            Ettersendingsmelding(packet).also {
                personMediator.håndter(it.ettersending, it)
            }
        }.also {
            Metrikker.ettersendingForsinkelse(packet["datoRegistrert"].asLocalDateTime())
        }
    }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
        metadata: MessageMetadata,
    ) {
        logg.debug { problems }
    }
}
