package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.helse.rapids_rivers.JsonMessage

internal class QuizSøknadMelding(packet: JsonMessage) : SøknadMelding(packet) {

    companion object {
        const val søknadIdNøkkel = "søknadsData.søknad_uuid"
    }
    override val søknadId = packet[søknadIdNøkkel].asText()
    override val søknad
        get() = Søknad(
            søknadId = søknadId,
            journalpostId = journalpostId,
            skjemaKode = skjemaKode,
            søknadsType = søknadsType,
            kanal = Kanal.Digital,
            datoInnsendt = datoRegistrert,
            vedlegg = vedlegg,
            tittel = tittel
        )
}
