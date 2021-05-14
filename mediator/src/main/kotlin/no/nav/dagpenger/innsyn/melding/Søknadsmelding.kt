package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.Companion.søknadOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.helse.rapids_rivers.JsonMessage

internal class Søknadsmelding(packet: JsonMessage) : Innsendingsmelding(packet) {
    private val søknadId = packet["søknadsData.brukerBehandlingId"].asText()
    internal val søknad
        get() = Søknad(
            søknadId,
            journalpostId,
            setOf(søknadOppgave.ferdig(søknadId, "Du har søkt dagpenger", datoRegistrert)) + oppgaver
        )
}
