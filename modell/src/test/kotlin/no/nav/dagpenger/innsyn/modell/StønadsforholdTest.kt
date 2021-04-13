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
        Stønadsforhold().apply { håndter(søknad) }.also {
            assertEquals(1, StønadsforholdInspektør(it).antallHendelser)
            assertEquals(TilstandType.UNDER_BEHANDLING, StønadsforholdInspektør(it).tilstand.type)
            it.håndter(ettersending).apply {
                assertEquals(2, StønadsforholdInspektør(it).antallHendelser)
                assertEquals(TilstandType.UNDER_BEHANDLING, StønadsforholdInspektør(it).tilstand.type)
            }
            it.håndter(innvilgetVedtak).apply {
                assertEquals(3, StønadsforholdInspektør(it).antallHendelser)
                assertEquals(TilstandType.LØPENDE, StønadsforholdInspektør(it).tilstand.type)
            }
            it.håndter(stansVedtak).apply {
                assertEquals(4, StønadsforholdInspektør(it).antallHendelser)
                assertEquals(TilstandType.STANSET, StønadsforholdInspektør(it).tilstand.type)
            }
        }
    }

    @Test
    fun `Følgende skal kaste feil i status start`() {
        assertThrows<IllegalStateException> { Stønadsforhold().håndter(innvilgetVedtak) }
        assertThrows<IllegalStateException> { Stønadsforhold().håndter(ettersending) }
        assertThrows<IllegalStateException> { Stønadsforhold().håndter(saksbehandling) }
    }

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

    val søknad = Søknad("1", emptySet())
    val ettersending = Ettersending("1", emptySet())
    val saksbehandling = Saksbehandling("3", "1", emptySet())
    val mangelbrev = Mangelbrev("3", "1", emptySet())
    val innvilgetVedtak = Vedtak("2", "1", emptySet(), Vedtak.Status.INNVILGET)
    val stansVedtak = Vedtak("2", "1", emptySet(), Vedtak.Status.STANS)
    val endringsVedtak = Vedtak("2", "1", emptySet(), Vedtak.Status.ENDRING)
}
