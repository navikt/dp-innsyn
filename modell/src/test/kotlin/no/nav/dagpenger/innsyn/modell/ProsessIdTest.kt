package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Journalføring
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class ProsessIdTest {

    @Test
    fun `Like stønadsider skal være like `() {
        val uuid = UUID.randomUUID()
        assertEquals(ProsessId(uuid, mutableListOf()), ProsessId(uuid, mutableListOf()))
    }

    @Test
    fun `skal håndtere ettersendinger tilhørende rett søknad`() {
        val hendelse = Søknad("1", "journalpostId", emptySet())
        val stønadsid = ProsessId()

        assertTrue(stønadsid.håndter(hendelse))
        assertTrue(stønadsid.håndter(hendelse))

        val annenEttersending = Ettersending("2", emptySet())
        assertFalse(stønadsid.håndter(annenEttersending))

        val ettersending = Ettersending("1", emptySet())
        assertTrue(stønadsid.håndter(ettersending))
    }

    @Test
    fun `skal håndtere vedtak tilhørende rett søknad`() {
        val søknad = Søknad("1", "journalpostId", emptySet())
        val journalført = Journalføring("journalpostId", "9", emptySet())
        val stønadsid = ProsessId()

        assertTrue(stønadsid.håndter(søknad))
        assertTrue(stønadsid.håndter(journalført))

        val annetVedtak = Vedtak("2", "5", Vedtak.Status.ENDRING)
        assertFalse(stønadsid.håndter(annetVedtak))

        val vedtak = Vedtak("1", "9", Vedtak.Status.INNVILGET)
        assertTrue(stønadsid.håndter(vedtak))
    }
}
