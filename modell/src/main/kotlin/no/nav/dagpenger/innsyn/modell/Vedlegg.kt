package no.nav.dagpenger.innsyn.modell

import java.time.LocalDate

internal class Vedlegg private constructor(
    private val id: String,
    private val frist: LocalDate,
    private var tilstand: Tilstand
) {
    constructor(id: String) : this(id, LocalDate.now().plusDays(14), IkkeInnsendt)

    companion object {
        fun erInnsendt(vedlegg: Vedlegg): Boolean = vedlegg.tilstand == Innsendt
    }

    fun håndter(ettersending: Ettersending) {
        if (this in ettersending) {
            tilstand = Innsendt
        }
    }

    internal interface Tilstand
    internal object IkkeInnsendt : Tilstand
    internal object Innsendt : Tilstand

    override operator fun equals(other: Any?) = other is Vedlegg && this.id == other.id
}
