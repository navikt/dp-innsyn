package no.nav.dagpenger.innsyn.tjenester

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.innsyn.PersonMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall.OppgaveMottak")

internal class OppgaveMottak(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator,
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("table", "SIAMO.OPPGAVE_LOGG") }
            validate { it.demandValue("after.SAK_TYPE", "DAGP") }
            validate {
                it.requireKey(
                    "after.SAK_ID",
                    "after.USERNAME",
                    "after.DUEDATE",
                    "after.KONTOR",
                    "after.OPPGAVETYPE_BESKRIVELSE",
                    "after.OPERASJON",
                    "after.TRANS_ID",
                )
            }
            validate { it.requireKey("after") }
        }.register(this)
    }

    companion object {
        private var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val sakId = packet["after"]["SAK_ID"].asText()
        val transaksjonId = packet["after"]["TRANS_ID"].asText()
        val benk = packet["after"]["KONTOR"].asText()

        withLoggingContext(
            "sakId" to sakId,
            "benk" to benk,
            "transaksjonId" to transaksjonId,
        ) {
            val operasjon = packet["after"]["OPERASJON"].asText()
            val navn = packet["after"]["OPPGAVETYPE_BESKRIVELSE"].asText()
            val frist = packet["after"]["DUEDATE"].asText().let { LocalDateTime.parse(it, formatter) }
            val dagerTil = Duration.between(LocalDateTime.now(), frist).toDays()

            logg.info { "Mottok operasjon $operasjon på oppgave $navn. Ligger på benk $benk, med frist til ${frist.toLocalDate()} (om $dagerTil dager)." }
            // sikkerlogg.info { "Mottok oppgave: ${packet.toJson()}" }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logg.debug { problems }
    }
}
