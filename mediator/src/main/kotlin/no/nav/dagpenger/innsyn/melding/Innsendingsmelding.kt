package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.OppgaveType.Companion.vedlegg
import no.nav.helse.rapids_rivers.JsonMessage

internal abstract class Innsendingsmelding(packet: JsonMessage) : Hendelsemelding(packet) {
    override val fødselsnummer = packet["naturligIdent"].asText()
    protected val oppgaver = packet["søknadsdata"]["vedlegg"].map {
        Oppgave(it["vedleggId"].asText(), vedlegg)
    }
}
