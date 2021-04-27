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
import no.nav.dagpenger.innsyn.modell.serde.StønadsforholdVisitor

class Stønadsforhold private constructor(
    private val stønadsid: Stønadsid,
    private val tidslinje: MutableSet<Oppgave>,
    private var tilstand: Tilstand
) {
    constructor() : this(
        stønadsid = Stønadsid(),
        tidslinje = mutableSetOf(),
        tilstand = Start
    )

    fun accept(visitor: StønadsforholdVisitor) {
        visitor.preVisit(this, tilstand)
        stønadsid.accept(visitor)
        tidslinje.forEach { it.accept(visitor) }
        visitor.postVisit(this, tilstand)
    }

    fun håndter(hendelse: Hendelse): Boolean {
        return when (hendelse) {
            is Søknad -> håndter(hendelse)
            is Ettersending -> håndter(hendelse)
            is Vedtak -> håndter(hendelse)
            is Mangelbrev -> håndter(hendelse)
            is Saksbehandling -> håndter(hendelse)
            else -> false
        }
    }

    fun håndter(søknad: Søknad): Boolean {
        if (!stønadsid.håndter(søknad)) return false
        return tilstand.håndter(this, søknad)
    }

    fun håndter(ettersending: Ettersending): Boolean {
        if (!stønadsid.håndter(ettersending)) return false
        return tilstand.håndter(this, ettersending)
    }

    fun håndter(vedtak: Vedtak): Boolean {
        if (!stønadsid.håndter(vedtak)) return false
        return tilstand.håndter(this, vedtak)
    }

    fun håndter(mangelbrev: Mangelbrev): Boolean {
        if (!stønadsid.håndter(mangelbrev)) return false
        return tilstand.håndter(this, mangelbrev)
    }

    fun håndter(saksbehandling: Saksbehandling): Boolean {
        if (!stønadsid.håndter(saksbehandling)) return false
        return tilstand.håndter(this, saksbehandling)
    }

    interface Tilstand {
        val type: TilstandType

        fun håndter(stønadsforhold: Stønadsforhold, søknad: Søknad) = false

        fun håndter(stønadsforhold: Stønadsforhold, ettersending: Ettersending) = false

        fun håndter(stønadsforhold: Stønadsforhold, vedtak: Vedtak) = false

        fun håndter(stønadsforhold: Stønadsforhold, mangelbrev: Mangelbrev) = false

        fun håndter(stønadsforhold: Stønadsforhold, saksbehandling: Saksbehandling) = false
    }

    internal object Start : Tilstand {
        override val type = START

        override fun håndter(stønadsforhold: Stønadsforhold, søknad: Søknad): Boolean {
            stønadsforhold.tilstand(søknad, UnderBehandling)
            stønadsforhold.tidslinje.addAll(søknad.oppgaver)
            return true
        }
    }

    internal object UnderBehandling : Tilstand {
        override val type = UNDER_BEHANDLING

        override fun håndter(stønadsforhold: Stønadsforhold, ettersending: Ettersending): Boolean {
            stønadsforhold.tidslinje.removeAll(ettersending.oppgaver)
            stønadsforhold.tidslinje.addAll(ettersending.oppgaver)
            return true
        }

        override fun håndter(stønadsforhold: Stønadsforhold, vedtak: Vedtak): Boolean {
            if (vedtak.status != Vedtak.Status.INNVILGET && vedtak.status != Vedtak.Status.AVSLÅTT) {
                throw IllegalStateException("Forventet ikke vedtak av typen ${vedtak.status.name} i tilstanden ${type.name}")
            }

            stønadsforhold.tidslinje.removeAll(vedtak.oppgaver)
            if (vedtak.status == Vedtak.Status.INNVILGET) {
                stønadsforhold.tilstand(vedtak, Løpende)
                stønadsforhold.tidslinje.addAll(vedtak.oppgaver)
            }
            if (vedtak.status == Vedtak.Status.AVSLÅTT) {
                stønadsforhold.tilstand(vedtak, Avslått)
                stønadsforhold.tidslinje.addAll(vedtak.oppgaver)
            }
            return true
        }

        override fun håndter(stønadsforhold: Stønadsforhold, mangelbrev: Mangelbrev): Boolean {
            // Noe mangler, lag oppgave og vent på ettersending
            return true
        }
    }

    internal object Løpende : Tilstand {
        override val type = LØPENDE

        override fun håndter(stønadsforhold: Stønadsforhold, vedtak: Vedtak) =
            // Endring/stans -> Løpende/Stanset/Avsluttet
            when (vedtak.status) {
                Vedtak.Status.STANS -> {
                    stønadsforhold.tilstand(vedtak, Stanset)
                    stønadsforhold.tidslinje.addAll(vedtak.oppgaver)
                    true
                }
                else -> false
            }

        override fun håndter(stønadsforhold: Stønadsforhold, mangelbrev: Mangelbrev): Boolean {
            throw IllegalStateException("Forventet ikke mangelbrev i tilstanden ${type.name}")
        }
    }

    internal object Stanset : Tilstand {
        override val type = STANSET

        override fun håndter(stønadsforhold: Stønadsforhold, søknad: Søknad): Boolean {
            // Gjenopptak? -> UnderBehandling
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, ettersending: Ettersending): Boolean {
            // Her skulle du nok sendt søknad først?
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, vedtak: Vedtak): Boolean {
            // Gjenopptak innvilget (bør ikke skje, saken skal være i UnderBehandling-state)
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, mangelbrev: Mangelbrev): Boolean {
            // Harru bomma? Søknad burde kommet først
            TODO("Not yet implemented")
        }
    }

    internal object Avslått : Tilstand {
        override val type = AVSLÅTT

        override fun håndter(stønadsforhold: Stønadsforhold, søknad: Søknad): Boolean {
            // Nytt stønadsforhold
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, ettersending: Ettersending): Boolean {
            // Ny vurdering -> UnderBehandling
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, vedtak: Vedtak): Boolean {
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, mangelbrev: Mangelbrev): Boolean {
            TODO("Not yet implemented")
        }
    }

    internal object Utløpt : Tilstand {
        override val type = UTLØPT

        override fun håndter(stønadsforhold: Stønadsforhold, søknad: Søknad): Boolean {
            // Feil.
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, ettersending: Ettersending): Boolean {
            // Feil.
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, vedtak: Vedtak): Boolean {
            // Feil.
            TODO("Not yet implemented")
        }

        override fun håndter(stønadsforhold: Stønadsforhold, mangelbrev: Mangelbrev): Boolean {
            // Feil.
            TODO("Not yet implemented")
        }
    }

    private fun tilstand(hendelse: Hendelse, nyTilstand: Tilstand) {
        if (tilstand == nyTilstand) return
        tilstand = nyTilstand
    }
}

