package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.helse.rapids_rivers.JsonMessage

internal class Ettersendingsmelding(packet: JsonMessage) : Innsendingsmelding(packet) {
    private val søknadsid = packet["brukerBehandlingId"].asText()
    private val behandlingskjedeId = packet["behandlingskjedeId"].asText()
    internal val ettersending get() = Ettersending(behandlingskjedeId, oppgaver)
}
