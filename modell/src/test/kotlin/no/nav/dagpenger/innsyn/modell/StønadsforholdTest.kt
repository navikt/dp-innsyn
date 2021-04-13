package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StønadsforholdTest {

    @Test
    fun `Skal gi feil når vedtak kommer i status Start`() {
        assertThrows<IllegalStateException> { Stønadsforhold().håndter(Vedtak("123")) }
    }

    @Test
    fun `Søknad skal flytte status fra start til underbehandling`() {
        Stønadsforhold().apply { håndter(søknad) }.also {
            assertEquals(TilstandType.UNDER_BEHANDLING, it.tilstand.type)
        }
    }

    @Test
    fun `Innvilget vedtak i under behandling skal gå til løpende`() {
        // MEN: det skal jo kunne gå til avslått også??
        Stønadsforhold().apply { håndter(søknad) }.also { it.håndter(vedtak) }.also {
            assertEquals(TilstandType.LØPENDE, it.tilstand.type)
        }
    }

    val søknad = Søknad("1", emptySet())
    val vedtak = Vedtak("2", "1", emptySet())
}
