package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.OppgaveType
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.helse.rapids_rivers.JsonMessage
import java.util.UUID

internal class Søknadsmelding(packet: JsonMessage) : Innsendingsmelding(packet) {
    private val søknadsid = packet["brukerBehandlingId"].asText()
    internal val søknad get() = Søknad(søknadsid, oppgaver + Oppgave(UUID.randomUUID().toString(), OppgaveType.vedtak))
}
