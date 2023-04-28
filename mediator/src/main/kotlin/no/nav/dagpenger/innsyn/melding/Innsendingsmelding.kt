package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Innsending.Vedlegg
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending.Vedlegg.Status
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.time.LocalDateTime

internal abstract class Innsendingsmelding(packet: JsonMessage) : Hendelsemelding(packet) {
    override val fødselsnummer = packet["fødselsnummer"].asText()
    internal val journalpostId: String = packet["journalpostId"].asText()
    internal val skjemaKode = packet["skjemaKode"].asText()
    internal val datoRegistrert: LocalDateTime = packet["datoRegistrert"].asLocalDateTime()
    internal val tittel = packet["tittel"].asText()

    internal val vedlegg = packet["søknadsData.vedlegg"].map {
        Vedlegg(
            it["skjemaNummer"].asText(),
            it["navn"].asText(),
            Status.valueOf(it["innsendingsvalg"].asText()),
        )
    }
}
