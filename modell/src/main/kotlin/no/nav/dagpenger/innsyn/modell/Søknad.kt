package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.Vedlegg.Tilstand.IkkeInnsendt

internal class Søknad(private val id: String, vedlegg: List<Vedlegg> = emptyList()) {
    fun håndter(ettersending: Ettersending) {
        if(ettersending.id != id) return
        vedlegg.forEach{it.håndter(ettersending)}
    }

    private val vedlegg: MutableList<Vedlegg> = vedlegg.toMutableList()

    val erKomplett: Boolean
        get() = vedlegg.none{ it.tilstand is IkkeInnsendt }

    val tilstand: Tilstand = Tilstand.Innsendt()

    abstract class Tilstand {
        class Innsendt : Tilstand()
    }


}
