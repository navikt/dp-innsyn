package no.nav.dagpenger.innsyn.melding

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal.Papir
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad

internal class PapirSøknadsMelding(
    packet: JsonMessage,
) : SøknadMelding(packet) {
    override val søknadId: String?
        get() = null

    override val søknad
        get() =
            Søknad(
                søknadId = søknadId,
                journalpostId = journalpostId,
                skjemaKode = skjemaKode,
                søknadsType = søknadsType,
                kanal = Papir,
                datoInnsendt = datoRegistrert,
                vedlegg = emptyList(),
                tittel = tittel,
            )
}
