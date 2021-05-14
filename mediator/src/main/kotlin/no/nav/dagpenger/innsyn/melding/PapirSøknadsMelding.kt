package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.PapirSøknad
import no.nav.helse.rapids_rivers.JsonMessage

internal class PapirSøknadsMelding(private val packet: JsonMessage) : Hendelsemelding(packet) {

    private val journalpostId = packet["journalpostId"].asText()
    internal val papirSøknad
        get() = PapirSøknad(
            journalpostId,
            setOf(Oppgave.OppgaveType.søknadOppgave.ferdig("EN ID", "Du har søkt dagpenger"))
        )
    override val fødselsnummer: String
        get() = packet["fødselsnummer"].asText()
}
