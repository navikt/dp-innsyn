package no.nav.dagpenger.innsyn.modell

import java.time.LocalDate

internal class Vedlegg(private val id: String, frist: LocalDate = LocalDate.now().plusDays(14)) {

    abstract class Tilstand {
        class IkkeInnsendt : Tilstand()
        class Innsendt : Tilstand()
    }
    var tilstand: Tilstand = Tilstand.IkkeInnsendt()
    val frist: LocalDate = frist

    fun håndter(ettersending: Ettersending) {
        if (this in ettersending) {
            tilstand = Tilstand.Innsendt()
        }
    }

    override operator fun equals(other: Any?) = other is Vedlegg && this.id == other.id
}
