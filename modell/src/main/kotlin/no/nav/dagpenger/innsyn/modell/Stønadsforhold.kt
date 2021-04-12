package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Mangelbrev
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Saksbehandling
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak

class Stønadsforhold private constructor(
    private val tidslinje: Tidslinje,
    private val oppgaver: List<Oppgave>,
    private val tilstand: Tilstand
) {
    constructor() : this(Tidslinje(), mutableListOf(), Start)

    fun håndter(søknad: Søknad) {
        tilstand.håndter(søknad)
    }

    fun håndter(ettersending: Ettersending) {
        tilstand.håndter(ettersending)
    }

    fun håndter(vedtak: Vedtak) {
        tilstand.håndter(vedtak)
    }

    fun håndter(mangelbrev: Mangelbrev) {
        tilstand.håndter(mangelbrev)
    }

    interface Tilstand {
        fun håndter(søknad: Søknad)
        fun håndter(ettersending: Ettersending)
        fun håndter(vedtak: Vedtak)
        fun håndter(mangelbrev: Mangelbrev)
        fun håndter(saksbehandling: Saksbehandling) {}
    }

    private object Start : Tilstand {
        override fun håndter(søknad: Søknad) {
            // Lag oppgaver for ettersending? Vedtak?
            // oppgaver.leggTil(søknad.oppgaver)
            // tidslinje.leggTil(Vedtak)
            // UnderBehandling
        }

        override fun håndter(ettersending: Ettersending) {
            // Feil.
        }

        override fun håndter(vedtak: Vedtak) {
            // Feil.
        }

        override fun håndter(mangelbrev: Mangelbrev) {
            // Feil.
        }
    }

    private object UnderBehandling : Tilstand {
        override fun håndter(søknad: Søknad) {
            // Nå har du bomma på redigeringsknappen
        }

        override fun håndter(ettersending: Ettersending) {
            // Vedlegg har blitt ettersendt, oppdater oppgaveliste
        }

        override fun håndter(vedtak: Vedtak) {
            // Vedtak er fattet, gå til løpende/avsluttet
        }

        override fun håndter(mangelbrev: Mangelbrev) {
            // Noe mangler, lag oppgave og vent på ettersending
        }
    }

    private object Løpende : Tilstand {
        override fun håndter(søknad: Søknad) {
            // Harru bomma?
            TODO("Not yet implemented")
        }

        override fun håndter(ettersending: Ettersending) {
            // Harru bomma?
            TODO("Not yet implemented")
        }

        override fun håndter(vedtak: Vedtak) {
            // Endring/stans -> Løpende/Stanset/Avsluttet
            TODO("Not yet implemented")
        }

        override fun håndter(mangelbrev: Mangelbrev) {
            // Harru bomma?
            TODO("Not yet implemented")
        }
    }

    private object Stanset : Tilstand {
        override fun håndter(søknad: Søknad) {
            // Gjenopptak? -> UnderBehandling
            TODO("Not yet implemented")
        }

        override fun håndter(ettersending: Ettersending) {
            // Her skulle du nok sendt søknad først?
            TODO("Not yet implemented")
        }

        override fun håndter(vedtak: Vedtak) {
            // Gjenopptak innvilget (bør ikke skje, saken skal være i UnderBehandling-state)
            TODO("Not yet implemented")
        }

        override fun håndter(mangelbrev: Mangelbrev) {
            // Harru bomma? Søknad burde kommet først
            TODO("Not yet implemented")
        }
    }

    private object Avslått : Tilstand {
        override fun håndter(søknad: Søknad) {
            // Nytt stønadsforhold
            TODO("Not yet implemented")
        }

        override fun håndter(ettersending: Ettersending) {
            // Ny vurdering -> UnderBehandling
            TODO("Not yet implemented")
        }

        override fun håndter(vedtak: Vedtak) {
            TODO("Not yet implemented")
        }

        override fun håndter(mangelbrev: Mangelbrev) {
            TODO("Not yet implemented")
        }
    }

    private object Utløpt : Tilstand {
        override fun håndter(søknad: Søknad) {
            // Feil.
            TODO("Not yet implemented")
        }

        override fun håndter(ettersending: Ettersending) {
            // Feil.
            TODO("Not yet implemented")
        }

        override fun håndter(vedtak: Vedtak) {
            // Feil.
            TODO("Not yet implemented")
        }

        override fun håndter(mangelbrev: Mangelbrev) {
            // Feil.
            TODO("Not yet implemented")
        }
    }
}
