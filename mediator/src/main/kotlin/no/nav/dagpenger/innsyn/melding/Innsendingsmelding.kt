package no.nav.dagpenger.innsyn.melding

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.innsyn.Dagpenger.vedleggOppgave
import no.nav.helse.rapids_rivers.JsonMessage

internal abstract class Innsendingsmelding(packet: JsonMessage) : Hendelsemelding(packet) {
    // override val fødselsnummer = packet["naturligIdent"].asText()
    override val fødselsnummer = packet["søknadsdata.aktoerId"].asText()
    protected val oppgaver = packet["søknadsdata.vedlegg"].map {
        val vedleggId = it["vedleggId"].asInt().toString()

        if (it["innsendingsvalg"].asText() == "LastetOpp") {
            vedleggOppgave.ferdig(vedleggId, beskrivelse(it))
        } else {
            vedleggOppgave.ny(vedleggId, beskrivelse(it))
        }
    }.toSet()

    private fun beskrivelse(vedlegg: JsonNode) = "${vedlegg["navn"]?.let { it.asText()}} (${vedlegg["skjemaNummer"].asText()})"
}
