package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.Vedlegg.Companion.erInnsendt as erVedleggInnsendt

internal class Søknad private constructor(
    private val id: String,
    private val vedlegg: MutableList<Vedlegg>,
    private var tilstand: Tilstand
) : Hendelse() {
    constructor(id: String) : this(id, mutableListOf(), Innsendt)
    constructor(id: String, vedlegg: List<Vedlegg>) : this(
        id,
        vedlegg.toMutableList(),
        Innsendt
    )

    companion object {
        fun erInnsendt(søknad: Søknad) = søknad.tilstand == Innsendt
        fun erKomplett(søknad: Søknad) = søknad.vedlegg.all(::erVedleggInnsendt)
    }

    internal fun håndter(ettersending: Ettersending) {
        tilstand.håndter(this, ettersending)
    }

    internal fun håndter(vedtak: Vedtak) {
        tilstand.håndter(this, vedtak)
    }

    internal interface Tilstand {
        fun håndter(søknad: Søknad, ettersending: Ettersending) {}
        fun håndter(søknad: Søknad, vedtak: Vedtak) {}
    }

    internal object Innsendt : Tilstand {
        override fun håndter(søknad: Søknad, vedtak: Vedtak) {
            søknad.tilstand = FerdigBehandlet
        }

        override fun håndter(søknad: Søknad, ettersending: Ettersending) {
        }
    }

    internal object UnderBehandling : Tilstand {
        override fun håndter(søknad: Søknad, vedtak: Vedtak) {
            søknad.tilstand = FerdigBehandlet
        }

        override fun håndter(søknad: Søknad, ettersending: Ettersending) {
            if (ettersending.søknadId != søknad.id) return
            søknad.vedlegg.forEach {
                it.håndter(ettersending)
            }
        }
    }

    internal object FerdigBehandlet : Tilstand
}
