package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Mangelbrev
import no.nav.dagpenger.innsyn.modell.hendelser.Saksbehandling
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.serde.StønadsforholdVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class StønadsforholdTest {
    @Test
    fun `Søknad, ettersending, vedtak skal gi hendelser i tidslinjen og endre tilstand`() {
        Stønadsforhold().also {
            it.håndter(søknad)
            inspektør(it) {
                assertEquals(TilstandType.UNDER_BEHANDLING, tilstand.type)
            }

            it.håndter(ettersending)
            inspektør(it) {
                assertEquals(TilstandType.UNDER_BEHANDLING, tilstand.type)
            }

            it.håndter(innvilgetVedtak)
            inspektør(it) {
                assertEquals(TilstandType.LØPENDE, tilstand.type)
            }

            it.håndter(stansVedtak)
            inspektør(it) {
                assertEquals(TilstandType.STANSET, tilstand.type)
            }
        }
    }

    @Test
    fun `Skal returnere false i tilstand start`() {
        assertFalse(Stønadsforhold().håndter(innvilgetVedtak))
        assertFalse(Stønadsforhold().håndter(ettersending))
        assertFalse(Stønadsforhold().håndter(saksbehandling))
    }

    private fun <R> inspektør(stønadsforhold: Stønadsforhold, block: StønadsforholdInspektør.() -> R): R =
        StønadsforholdInspektør(stønadsforhold).block()

    private class StønadsforholdInspektør(stønadsforhold: Stønadsforhold) : StønadsforholdVisitor {
        lateinit var tilstand: Stønadsforhold.Tilstand

        init {
            stønadsforhold.accept(this)
        }

        override fun preVisit(
            stønadsforhold: Stønadsforhold,
            tilstand: Stønadsforhold.Tilstand,
            opprettet: LocalDateTime,
            oppdatert: LocalDateTime
        ) {
            this.tilstand = tilstand
        }
    }

    private val søknad = Søknad("1", emptySet(), "")
    private val ettersending = Ettersending("1", emptySet())
    private val saksbehandling = Saksbehandling("3", "1", emptySet())
    private val mangelbrev = Mangelbrev("3", "1", emptySet())
    private val innvilgetVedtak = Vedtak("2", "1", emptySet(), Vedtak.Status.INNVILGET)
    private val stansVedtak = Vedtak("2", "1", emptySet(), Vedtak.Status.STANS)
    private val endringsVedtak = Vedtak("2", "1", emptySet(), Vedtak.Status.ENDRING)
}
