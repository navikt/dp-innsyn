package no.nav.dagpenger.innsyn.melding

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import no.nav.dagpenger.innsyn.modell.hendelser.Sakstilknytning

internal class Journalførtmelding(
    packet: JsonMessage,
) : Hendelsemelding(packet) {
    override val fødselsnummer: String = packet["fødselsnummer"].asText()
    private val journalpostId = packet["journalpostId"].asText()
    private val fagsakId = packet["fagsakId"].asText()
    internal val journalføring
        get() =
            Sakstilknytning(
                journalpostId,
                fagsakId,
            )
}
