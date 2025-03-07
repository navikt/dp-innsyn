package no.nav.dagpenger.innsyn.tjenester

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.innsyn.Metrikker
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.dagpenger.innsyn.melding.LegacySøknadsmelding
import no.nav.dagpenger.innsyn.melding.PapirSøknadsMelding
import no.nav.dagpenger.innsyn.melding.QuizSøknadMelding
import no.nav.dagpenger.innsyn.melding.SøknadMelding
import java.time.Duration
import java.time.LocalDateTime

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall.SøknadMottak")

internal class SøknadMottak(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                validate { it.demandValue("@event_name", "innsending_mottatt") }
                validate {
                    it.requireKey(
                        "fødselsnummer",
                        "journalpostId",
                        "datoRegistrert",
                        "skjemaKode",
                        "tittel",
                    )
                }
                validate { it.requireAny("type", listOf("NySøknad", "Gjenopptak")) }
                validate {
                    it.interestedIn(
                        "søknadsData.vedlegg",
                        QuizSøknadMelding.SØKNAD_ID_NØKKEL,
                        LegacySøknadsmelding.SØKNAD_ID_NØKKEL,
                    )
                }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val søknadMelding: SøknadMelding = packet.tilSøknadMelding()
        val fnr = packet["fødselsnummer"].asText()
        val søknadId = søknadMelding.søknadId
        val journalpostId = packet["journalpostId"].asText()

        withLoggingContext(
            "søknadId" to søknadId,
            "journalpostId" to journalpostId,
        ) {
            logg.info { "Mottok ny søknad av typen ${søknadMelding.javaClass.simpleName}." }
            sikkerlogg.info { "Mottok ny søknad for person $fnr: ${packet.toJson()}" }
            personMediator.håndter(søknadMelding.søknad, søknadMelding)
        }.also {
            logg.info {
                val datoRegistrert =
                    packet["datoRegistrert"].asLocalDateTime().also {
                        Metrikker.søknadForsinkelse(it)
                    }
                val forsinkelse = Duration.between(datoRegistrert, LocalDateTime.now()).toMillis()
                "Har lagret en søknad med $forsinkelse millisekunder forsinkelse"
            }
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

private fun JsonMessage.tilSøknadMelding(): SøknadMelding =
    if (this.harSøknadIdFraQuiz()) {
        QuizSøknadMelding(this)
    } else if (this.harSøknadIdFraLegacy()) {
        LegacySøknadsmelding(this)
    } else {
        PapirSøknadsMelding(this)
    }

private fun JsonMessage.harSøknadIdFraQuiz() = !this[QuizSøknadMelding.SØKNAD_ID_NØKKEL].isMissingNode

private fun JsonMessage.harSøknadIdFraLegacy() = !this[LegacySøknadsmelding.SØKNAD_ID_NØKKEL].isMissingNode
