package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Søknadsprosess
import no.nav.helse.rapids_rivers.JsonMessage

internal abstract class Innsendingsmelding(packet: JsonMessage) : Hendelsemelding(packet) {
    override val fødselsnummer = packet["aktoerId"].asText()
    protected val oppgaver = packet["vedlegg"].map {
        Oppgave(it["vedleggId"].asText())
    }
}

internal class Søknadsmelding(packet: JsonMessage) : Innsendingsmelding(packet) {
    private val søknadsid = packet["brukerBehandlingId"].asText()
    internal val søknad get() = Søknadsprosess(søknadsid, oppgaver)
}

internal class Ettersendingsmelding(packet: JsonMessage) : Innsendingsmelding(packet) {
    private val søknadsid = packet["brukerBehandlingId"].asText()
    private val behandlingskjedeId = packet["behandlingsskjedeId"].asText()
    internal val ettersending get() = Ettersending(behandlingskjedeId, oppgaver)
}
