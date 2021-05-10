package no.nav.dagpenger.innsyn.melding

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.Companion.vedleggOppgave
import no.nav.helse.rapids_rivers.JsonMessage

internal abstract class Innsendingsmelding(packet: JsonMessage) : Hendelsemelding(packet) {
    override val fødselsnummer = packet["fødselsnummer"].asText()
    protected val oppgaver = packet["søknadsData.vedlegg"].map {
        val vedleggId = it.vedleggIt

        if (it["innsendingsvalg"].asText() == "LastetOpp") {
            vedleggOppgave.ferdig(vedleggId, it.beskrivelse)
        } else {
            vedleggOppgave.ny(vedleggId, it.beskrivelse)
        }
    }.toSet()
}

private val JsonNode.vedleggIt
    get() = listOf(
        this["skjemaNummer"].asText(),
        navn
    ).joinToString(":")
private val JsonNode.beskrivelse
    get() = "$navn (${this["skjemaNummer"].asText()})"
private val JsonNode.navn
    get() = this["navn"]?.let { it.asText() }
