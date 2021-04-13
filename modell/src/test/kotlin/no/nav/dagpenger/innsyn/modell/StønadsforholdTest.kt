package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Hendelse
import no.nav.dagpenger.innsyn.modell.hendelser.Mangelbrev
import no.nav.dagpenger.innsyn.modell.hendelser.Saksbehandling
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.serde.StønadsforholdVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class StønadsforholdTest {
    @Test
    fun `Søknad, ettersending, vedtak skal gi hendelser i tidslinjen og endre tilstand`() {
        Stønadsforhold().also {
            it.håndter(søknad)
            inspektør(it) {
                assertEquals(1, antallHendelser)
                assertEquals(TilstandType.UNDER_BEHANDLING, tilstand.type)
            }

            it.håndter(ettersending)
            inspektør(it) {
                assertEquals(2, antallHendelser)
                assertEquals(TilstandType.UNDER_BEHANDLING, tilstand.type)
            }

            it.håndter(innvilgetVedtak)
            inspektør(it) {
                assertEquals(3, antallHendelser)
                assertEquals(TilstandType.LØPENDE, tilstand.type)
            }

            it.håndter(stansVedtak)
            inspektør(it) {
                assertEquals(4, antallHendelser)
                assertEquals(TilstandType.STANSET, tilstand.type)
            }
        }
    }

    @Test
    fun `Følgende skal kaste feil i status start`() {
        assertThrows<IllegalStateException> { Stønadsforhold().håndter(innvilgetVedtak) }
        assertThrows<IllegalStateException> { Stønadsforhold().håndter(ettersending) }
        assertThrows<IllegalStateException> { Stønadsforhold().håndter(saksbehandling) }
    }

    private fun <R> inspektør(stønadsforhold: Stønadsforhold, block: StønadsforholdInspektør.() -> R): R =
        StønadsforholdInspektør(stønadsforhold).block()

    private class StønadsforholdInspektør(stønadsforhold: Stønadsforhold) : StønadsforholdVisitor {
        var antallHendelser = 0
        lateinit var tilstand: Stønadsforhold.Tilstand

        init {
            stønadsforhold.accept(this)
        }

        override fun preVisit(hendelse: Hendelse, behandlingskjedeId: BehandlingskjedeId) {
            antallHendelser++
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

    private val søknad = Søknad("1", emptySet())
    private val ettersending = Ettersending("1", emptySet())
    private val saksbehandling = Saksbehandling("3", "1", emptySet())
    private val mangelbrev = Mangelbrev("3", "1", emptySet())
    private val innvilgetVedtak = Vedtak("2", "1", emptySet(), Vedtak.Status.INNVILGET)
    private val stansVedtak = Vedtak("2", "1", emptySet(), Vedtak.Status.STANS)
    private val endringsVedtak = Vedtak("2", "1", emptySet(), Vedtak.Status.ENDRING)
}
