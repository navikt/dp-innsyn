package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.Dagpenger.søknadOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.helse.rapids_rivers.JsonMessage

internal class Søknadsmelding(packet: JsonMessage) : Innsendingsmelding(packet) {
    private val søknadId = packet["søknadsData.brukerBehandlingId"].asText()
    private val journalpostId = packet["journalpostId"].asText()
    internal val søknad
        get() = Søknad(
            søknadId,
            journalpostId,
            setOf(søknadOppgave.ferdig(søknadId, "Du har søkt dagpenger")) + oppgaver
        )
}
