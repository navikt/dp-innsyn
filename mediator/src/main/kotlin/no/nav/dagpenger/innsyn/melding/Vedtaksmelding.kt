package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak.Status
import no.nav.helse.rapids_rivers.JsonMessage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class Vedtaksmelding(private val packet: JsonMessage) : Hendelsemelding(packet) {
    companion object {
        private var formatter1: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
        private var formatter2: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    private val vedtakId = packet["after"]["VEDTAK_ID"].asText()
    private val sakId = packet["after"]["SAK_ID"].asText()
    private val fattet = packet["op_ts"].asText().let {
        LocalDateTime.parse(it, formatter1)
    }
    private val fraDato = packet["after"]["FRA_DATO"].asText().let {
        LocalDateTime.parse(it, formatter2)
    }
    private val tilDato get() = when (packet["after"]["TIL_DATO"].asText()) {
        "null" -> null
        else -> LocalDateTime.parse(packet["after"]["TIL_DATO"].asText(), formatter2)
    }

    internal val vedtak
        get() = Vedtak(
            vedtakId = vedtakId,
            fagsakId = sakId,
            status = status,
            datoFattet = fattet,
            fraDato = fraDato,
            tilDato = tilDato
        )
    override val fødselsnummer: String = packet["tokens"]["FODSELSNR"].asText()
    private val status
        get() =
            when (packet["after"]["UTFALLKODE"].asText()) {
                "JA" -> Status.INNVILGET
                "NEI" -> Status.AVSLÅTT
                else -> throw IllegalArgumentException("Ukjent utfallskode")
            }
}
