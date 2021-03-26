package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.Dagpenger.vedleggOppgave
import no.nav.helse.rapids_rivers.JsonMessage

internal abstract class Innsendingsmelding(packet: JsonMessage) : Hendelsemelding(packet) {
    // override val fødselsnummer = packet["naturligIdent"].asText()
    override val fødselsnummer = packet["søknadsdata.aktoerId"].asText()
    protected val oppgaver = packet["søknadsdata.vedlegg"].map {
        val vedleggId = it["vedleggId"].asText()

        if (it["innsendingsvalg"].asText() == "LastetOpp") {
            vedleggOppgave.ferdig(vedleggId)
        } else {
            vedleggOppgave.ny(vedleggId)
        }
    }.toSet()
}
