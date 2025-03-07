package no.nav.dagpenger.innsyn.melding

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage

internal sealed class Hendelsemelding(
    private val packet: JsonMessage,
) {
    internal abstract val fødselsnummer: String
}
