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

class SøknadsprosessTest {
    @Test
    fun `Søknad, ettersending, vedtak skal gi hendelser i tidslinjen og endre tilstand`() {
        Søknadsprosess().also {
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
        assertFalse(Søknadsprosess().håndter(innvilgetVedtak))
        assertFalse(Søknadsprosess().håndter(ettersending))
        assertFalse(Søknadsprosess().håndter(saksbehandling))
    }

    private fun <R> inspektør(søknadsprosess: Søknadsprosess, block: StønadsforholdInspektør.() -> R): R =
        StønadsforholdInspektør(søknadsprosess).block()

    private class StønadsforholdInspektør(søknadsprosess: Søknadsprosess) : StønadsforholdVisitor {
        lateinit var tilstand: Søknadsprosess.Tilstand

        init {
            søknadsprosess.accept(this)
        }

        override fun preVisit(
            søknadsprosess: Søknadsprosess,
            tilstand: Søknadsprosess.Tilstand
        ) {
            this.tilstand = tilstand
        }
    }

    private val søknad = Søknad("1", "journalpostId", emptySet())
    private val ettersending = Ettersending("1", emptySet())
    private val saksbehandling = Saksbehandling("3", "1", emptySet())
    private val mangelbrev = Mangelbrev("3", "1", emptySet())
    private val innvilgetVedtak = Vedtak("2", "1", emptySet(), Vedtak.Status.INNVILGET)
    private val stansVedtak = Vedtak("2", "1", emptySet(), Vedtak.Status.STANS)
    private val endringsVedtak = Vedtak("2", "1", emptySet(), Vedtak.Status.ENDRING)
}
