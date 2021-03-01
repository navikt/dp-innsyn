package no.nav.dagpenger.innsyn.meldinger

import no.nav.helse.rapids_rivers.JsonMessage
import java.time.LocalDateTime
import java.util.UUID

internal class ØnskerStatusMelding(private val fødselsnummer: String) {
    private val navn = "status"
    private val opprettet = LocalDateTime.now()
    private val id = UUID.randomUUID()
    private val avklaringsId = UUID.randomUUID()

    fun toJson() = JsonMessage.newMessage(
        mutableMapOf(
            "@behov" to navn,
            "@opprettet" to opprettet,
            "@id" to id,
            "avklaringsId" to avklaringsId,
            "fødselsnummer" to fødselsnummer,
        )
    ).toJson()
}