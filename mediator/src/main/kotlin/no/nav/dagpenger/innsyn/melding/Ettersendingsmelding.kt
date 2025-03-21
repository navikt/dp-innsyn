package no.nav.dagpenger.innsyn.melding

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal.Digital
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal.Papir

internal class Ettersendingsmelding(
    packet: JsonMessage,
) : Innsendingsmelding(packet) {
    private val behandlingskjedeId = packet["søknadsData.behandlingskjedeId"].asText()
    private val ettersendingId = packet["søknadsData.brukerBehandlingId"].asText()
    private val kanal = ettersendingId?.let { Digital } ?: Papir

    internal val ettersending get() =
        Ettersending(
            ettersendingId,
            behandlingskjedeId,
            journalpostId,
            kanal,
            vedlegg,
        )
}
