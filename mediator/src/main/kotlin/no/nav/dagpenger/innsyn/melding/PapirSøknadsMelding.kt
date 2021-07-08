package no.nav.dagpenger.innsyn.melding

import no.nav.dagpenger.innsyn.modell.hendelser.Kanal.Papir
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.helse.rapids_rivers.JsonMessage

internal class PapirSøknadsMelding(packet: JsonMessage) : Innsendingsmelding(packet) {
    private val søknadsType = Søknad.SøknadsType.valueOf(packet["type"].asText())

    internal val søknad
        get() = Søknad(
            søknadId = null,
            journalpostId = journalpostId,
            skjemaKode = skjemaKode,
            søknadsType = søknadsType,
            kanal = Papir,
            datoInnsendt = datoRegistrert,
            vedlegg = emptyList()
        )
}
