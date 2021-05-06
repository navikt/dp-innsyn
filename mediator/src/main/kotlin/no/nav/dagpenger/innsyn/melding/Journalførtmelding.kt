package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.Dagpenger.vedtakOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Journalføring
import no.nav.helse.rapids_rivers.JsonMessage

internal class Journalførtmelding(private val packet: JsonMessage) : Hendelsemelding(packet) {
    override val fødselsnummer: String = packet["fødselsnummer"].asText()
    private val journalpostId = packet["journalpostId"].asText()
    private val fagsakId = packet["fagsakId"].asText()
    internal val journalføring
        get() = Journalføring(
            journalpostId,
            fagsakId,
            setOf(vedtakOppgave.ny(fagsakId, "Venter på vedtak")),
        )
}
