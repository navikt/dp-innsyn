package no.nav.dagpenger.innsyn.melding

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.Companion.vedleggOppgave
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.time.LocalDateTime

internal abstract class Innsendingsmelding(packet: JsonMessage) : Hendelsemelding(packet) {
    override val fødselsnummer = packet["fødselsnummer"].asText()
    internal val journalpostId: String = packet["journalpostId"].asText()
    internal val datoRegistrert: LocalDateTime = packet["datoRegistrert"].asLocalDateTime()
    protected val oppgaver = packet["søknadsData.vedlegg"].map {
        val vedleggId = it.vedleggIt

        if (it["innsendingsvalg"].asText() == "LastetOpp") {
            vedleggOppgave.ferdig(vedleggId, it.beskrivelse, datoRegistrert)
        } else {
            vedleggOppgave.ny(vedleggId, it.beskrivelse, datoRegistrert)
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
