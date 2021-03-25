package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.Dagpenger.vedtakOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.helse.rapids_rivers.JsonMessage

internal class Søknadsmelding(packet: JsonMessage) : Innsendingsmelding(packet) {
    private val søknadId = packet["søknadsdata.brukerBehandlingId"].asText()
    internal val søknad get() = Søknad(søknadId, (oppgaver + vedtakOppgave.ny(søknadId)))
}
