package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.Vedlegg.Tilstand.IkkeInnsendt

internal class Søknad(private val id: String, vedlegg: List<Vedlegg> = emptyList()) {
    var tilstand: Tilstand = Tilstand.Innsendt()
    private val vedlegg: MutableList<Vedlegg> = vedlegg.toMutableList()
    val erKomplett: Boolean
        get() = vedlegg.none { it.tilstand is IkkeInnsendt }

    fun håndter(ettersending: Ettersending) {
        if (ettersending.id != id) return
        vedlegg.håndter(ettersending)
    }

    fun håndter(vedtak: Vedtak) {
        tilstand = Tilstand.FerdigBehandlet()
    }

    abstract class Tilstand {
        class Innsendt : Tilstand()
        class UnderBehandling : Tilstand()
        class FerdigBehandlet : Tilstand()
    }
}

private fun List<Vedlegg>.håndter(ettersending: Ettersending) {
    this.forEach {
        // if (it !in ettersending)
        it.håndter(ettersending)
    }
}
