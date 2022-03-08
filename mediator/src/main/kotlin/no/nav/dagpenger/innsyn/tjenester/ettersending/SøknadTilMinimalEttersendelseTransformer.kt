package no.nav.dagpenger.innsyn.tjenester.ettersending

import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.serde.SøknadVisitor
import java.time.LocalDateTime

class SøknadTilMinimalEttersendelseTransformer(søknader: List<Søknad>) : SøknadVisitor {

    private val resultat = mutableListOf<MinimalEttersendingDto>()

    fun resultat() = resultat

    init {
        søknader.forEach { søknad ->
            søknad.accept(this)
        }
    }

    override fun visitSøknad(
        søknadId: String?,
        journalpostId: String,
        skjemaKode: String?,
        søknadsType: Søknad.SøknadsType,
        kanal: Kanal,
        datoInnsendt: LocalDateTime,
        tittel: String?
    ) {
        if (kanal == Kanal.Digital) {
            val ettersending = MinimalEttersendingDto(
                søknadId ?: throw IllegalArgumentException("SøknadId må være satt."),
                datoInnsendt.toZonedDateTimeInOslo(),
                tittel ?: throw IllegalArgumentException("Tittel må være satt.")
            )

            resultat.add(ettersending)
        }
    }
}
