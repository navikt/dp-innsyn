package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Kanal.Digital
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.helse.rapids_rivers.JsonMessage

internal class LegacySøknadsmelding(packet: JsonMessage) : SøknadMelding(packet) {
    companion object {
        const val SØKNAD_ID_NØKKEL = "søknadsData.brukerBehandlingId"
    }

    override val søknadId = packet[SØKNAD_ID_NØKKEL].asText()
    override val søknad
        get() =
            Søknad(
                søknadId = søknadId,
                journalpostId = journalpostId,
                skjemaKode = skjemaKode,
                søknadsType = søknadsType,
                kanal = Digital,
                datoInnsendt = datoRegistrert,
                vedlegg = vedlegg,
                tittel = tittel,
            )
}
