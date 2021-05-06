package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.helse.rapids_rivers.JsonMessage

internal class Ettersendingsmelding(packet: JsonMessage) : Innsendingsmelding(packet) {
    private val behandlingskjedeId = packet["søknadsData.behandlingskjedeId"].asText()
    internal val ettersending get() = Ettersending(behandlingskjedeId, oppgaver)
}
