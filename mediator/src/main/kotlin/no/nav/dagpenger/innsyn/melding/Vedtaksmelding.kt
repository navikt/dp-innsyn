package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.Dagpenger.vedtakOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.helse.rapids_rivers.JsonMessage

internal class Vedtaksmelding(packet: JsonMessage) : Hendelsemelding(packet) {

    private val vedtakId = packet["vedtakId"].asText()
    private val søknadId = packet["søknadId"].asText()
    internal val vedtak get() = Vedtak(vedtakId, søknadId, setOf(vedtakOppgave.ferdig(søknadId, "")))

    override val fødselsnummer: String = packet["fødselsnummer"].asText()
}
