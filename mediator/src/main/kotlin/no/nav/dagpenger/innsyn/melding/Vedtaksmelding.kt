package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak.Status
import no.nav.helse.rapids_rivers.JsonMessage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class Vedtaksmelding(private val packet: JsonMessage) : Hendelsemelding(packet) {
    companion object {
        private var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
    }

    private val vedtakId = packet["after"]["VEDTAK_ID"].asText()
    private val sakId = packet["after"]["SAK_ID"].asText()
    private val fattet = packet["op_ts"].asText().let {
        LocalDateTime.parse(it, formatter)
    }
    internal val vedtak get() = Vedtak(vedtakId, sakId, status)
    override val fødselsnummer: String = packet["tokens"]["FODSELSNR"].asText()
    private val status
        get() =
            when (packet["after"]["UTFALLKODE"].asText()) {
                "JA" -> Status.INNVILGET
                "NEI" -> Status.AVSLÅTT
                else -> throw IllegalArgumentException("Ukjent utfallskode")
            }
}
