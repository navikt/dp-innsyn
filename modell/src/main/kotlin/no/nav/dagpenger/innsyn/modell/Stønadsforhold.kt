package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.TilstandType.AVSLÅTT
import no.nav.dagpenger.innsyn.modell.TilstandType.LØPENDE
import no.nav.dagpenger.innsyn.modell.TilstandType.STANSET
import no.nav.dagpenger.innsyn.modell.TilstandType.START
import no.nav.dagpenger.innsyn.modell.TilstandType.UNDER_BEHANDLING
import no.nav.dagpenger.innsyn.modell.TilstandType.UTLØPT
import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Hendelse
import no.nav.dagpenger.innsyn.modell.hendelser.Mangelbrev
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Saksbehandling
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import java.time.LocalDateTime

class Stønadsforhold private constructor(
    public val tidslinje: Tidslinje,
    public val oppgaver: List<Oppgave>,
    public var tilstand: Tilstand,
    private var oppdatert: LocalDateTime
) {
    constructor() : this(Tidslinje(), mutableListOf(), Start, LocalDateTime.now())

    fun håndter(søknad: Søknad) {
        tilstand.håndter(this, søknad)
    }

    fun håndter(ettersending: Ettersending) {
        tilstand.håndter(this, ettersending)
    }

    fun håndter(vedtak: Vedtak) {
        tilstand.håndter(this, vedtak)
    }

    fun håndter(mangelbrev: Mangelbrev) {
        tilstand.håndter(this, mangelbrev)
    }

    fun håndter(saksbehandling: Saksbehandling) {
        tilstand.håndter(this, saksbehandling)
    }

    interface Tilstand {
        val type: TilstandType
        fun håndter(stønadsforhold: Stønadsforhold, søknad: Søknad) {
            throw IllegalStateException("forventet ikke søknad i tilstanden ${type.name}")
        }

        fun håndter(stønadsforhold: Stønadsforhold, ettersending: Ettersending) {
            throw IllegalStateException("forventet ikke ettersending i tilstanden ${type.name}")
        }

        fun håndter(stønadsforhold: Stønadsforhold, vedtak: Vedtak) {
            throw IllegalStateException("forventet ikke vedtak i tilstanden ${type.name}")
        }

        fun håndter(stønadsforhold: Stønadsforhold, mangelbrev: Mangelbrev) {
            throw IllegalStateException("forventet ikke mangelbrev i tilstanden ${type.name}")
        }

        fun håndter(stønadsforhold: Stønadsforhold, saksbehandling: Saksbehandling) {
            throw IllegalStateException("forventet ikke saksbehandling i tilstanden ${type.name}")
        }
    }

    private object Start : Tilstand {
        override val type = START

        override fun håndter(stønadsforhold: Stønadsforhold, søknad: Søknad) {

            stønadsforhold.tilstand(søknad, UnderBehandling)

            // Lag oppgaver for ettersending? Vedtak?
            // oppgaver.leggTil(søknad.oppgaver)
            // tidslinje.leggTil(Vedtak)
            // UnderBehandling
        }
    }

    private object UnderBehandling : Tilstand {
        override val type = UNDER_BEHANDLING

        override fun håndter(stønadsforhold: Stønadsforhold, søknad: Søknad) {
            // Nå har du bomma på redigeringsknappen
        }

        override fun håndter(stønadsforhold: Stønadsforhold, ettersending: Ettersending) {
            // Vedlegg har blitt ettersendt, oppdater oppgaveliste
        }

        override fun håndter(stønadsforhold: Stønadsforhold, vedtak: Vedtak) {

            if (vedtak.status == Vedtak.Status.INNVILGET) stønadsforhold.tilstand(vedtak, Løpende)
            if (vedtak.status == Vedtak.Status.AVSLÅTT) stønadsforhold.tilstand(vedtak, Avslått)

            // Vedtak er fattet, gå til løpende/avsluttet
        }

        override fun håndter(stønadsforhold: Stønadsforhold, mangelbrev: Mangelbrev) {
            // Noe mangler, lag oppgave og vent på ettersending
        }
    }

    private object Løpende : Tilstand {
        override val type = LØPENDE

        override fun håndter(stønadsforhold: Stønadsforhold, søknad: Søknad) {
            // Harru bomma?
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, ettersending: Ettersending) {
            // Harru bomma?
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, vedtak: Vedtak) {
            // Endring/stans -> Løpende/Stanset/Avsluttet
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, mangelbrev: Mangelbrev) {
            // Harru bomma?
            TODO("Not yet implemented")
        }
    }

    private object Stanset : Tilstand {
        override val type = STANSET

        override fun håndter(stønadsforhold: Stønadsforhold, søknad: Søknad) {
            // Gjenopptak? -> UnderBehandling
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, ettersending: Ettersending) {
            // Her skulle du nok sendt søknad først?
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, vedtak: Vedtak) {
            // Gjenopptak innvilget (bør ikke skje, saken skal være i UnderBehandling-state)
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, mangelbrev: Mangelbrev) {
            // Harru bomma? Søknad burde kommet først
            TODO("Not yet implemented")
        }
    }

    private object Avslått : Tilstand {
        override val type = AVSLÅTT

        override fun håndter(stønadsforhold: Stønadsforhold, søknad: Søknad) {
            // Nytt stønadsforhold
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, ettersending: Ettersending) {
            // Ny vurdering -> UnderBehandling
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, vedtak: Vedtak) {
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, mangelbrev: Mangelbrev) {
            TODO("Not yet implemented")
        }
    }

    private object Utløpt : Tilstand {
        override val type = UTLØPT

        override fun håndter(stønadsforhold: Stønadsforhold, søknad: Søknad) {
            // Feil.
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, ettersending: Ettersending) {
            // Feil.
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, vedtak: Vedtak) {
            // Feil.
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, mangelbrev: Mangelbrev) {
            // Feil.
            TODO("Not yet implemented")
        }
    }

    private fun tilstand(hendelse: Hendelse, nyTilstand: Tilstand) {
        if (tilstand == nyTilstand) return
        tilstand = nyTilstand
        oppdatert = LocalDateTime.now()
    }
}
