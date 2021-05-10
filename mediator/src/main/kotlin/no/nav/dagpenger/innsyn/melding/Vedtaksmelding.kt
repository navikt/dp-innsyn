package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.Companion.vedtakOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.helse.rapids_rivers.JsonMessage

internal class Vedtaksmelding(private val packet: JsonMessage) : Hendelsemelding(packet) {

    private val vedtakId = packet["after"]["VEDTAK_ID"].asText()
    private val sakId = packet["after"]["SAK_ID"].asText()
    internal val vedtak get() = Vedtak(vedtakId, sakId, setOf(vedtakOppgave.ferdig(sakId, "")), status)

    override val fødselsnummer: String = packet["tokens"]["FODSELSNR"].asText()

    private val status
        get() =
            when (packet["after"]["UTFALLKODE"].asText()) {
                "JA" -> Vedtak.Status.INNVILGET
                "NEI" -> Vedtak.Status.AVSLÅTT
                else -> throw IllegalArgumentException("Ukjent utfallskode")
            }
}
