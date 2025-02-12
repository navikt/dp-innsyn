package no.nav.dagpenger.innsyn.melding

import no.nav.helse.rapids_rivers.JsonMessage

internal sealed class Hendelsemelding(
    private val packet: JsonMessage,
) {
    internal abstract val fødselsnummer: String
}
