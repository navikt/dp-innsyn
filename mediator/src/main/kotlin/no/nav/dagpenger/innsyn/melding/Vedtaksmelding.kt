package no.nav.dagpenger.innsyn.melding

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak.Status
import no.nav.dagpenger.innsyn.tjenester.fødselsnummer
import no.nav.helse.rapids_rivers.JsonMessage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class Vedtaksmelding(private val packet: JsonMessage) : Hendelsemelding(packet) {
    companion object {
        private var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]")
        private fun JsonNode.asArenaDato() =
            asText().let { LocalDateTime.parse(it, formatter) }
        private fun JsonNode.asOptionalArenaDato() =
            takeIf(JsonNode::isTextual)?.asText()?.takeIf(String::isNotEmpty)
                ?.let { LocalDateTime.parse(it, formatter) }
    }

    private val vedtakId = packet["after"]["VEDTAK_ID"].asText()
    private val sakId = packet["after"]["SAK_ID"].asText()

    private val fattet = packet["op_ts"].asArenaDato()
    private val fraDato = packet["after"]["FRA_DATO"].asArenaDato()
    private val tilDato = packet["after"]["TIL_DATO"].asOptionalArenaDato()

    internal val vedtak
        get() = Vedtak(
            vedtakId = vedtakId,
            fagsakId = sakId,
            status = status,
            datoFattet = fattet,
            fraDato = fraDato,
            tilDato = tilDato,
        )
    override val fødselsnummer: String = packet.fødselsnummer()
    private val status
        get() =
            when (packet["after"]["UTFALLKODE"].asText()) {
                "JA" -> Status.INNVILGET
                "NEI" -> Status.AVSLÅTT
                else -> throw IllegalArgumentException("Ukjent utfallskode")
            }
}
