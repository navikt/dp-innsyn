package no.nav.dagpenger.innsyn.behandlingsstatus

import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.FerdigBehandlet
import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.Ukjent
import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.UnderBehandling
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BehandlingsstatusTest {

    @Test
    fun `Behandlingsstatus er null når det er 0 søknader og 0 vedtak`() {
        assertEquals(null, Behandlingsstatus(antallSøknader = 0, antallVedtak = 0).antattStatus)
    }

    @Test
    fun `Behandlingsstatus er UnderBehandling når det finnes 1 søknad og 0 vedtak`() {
        assertEquals(
            UnderBehandling,
            Behandlingsstatus(antallSøknader = 1, antallVedtak = 0).antattStatus,
        )
    }

    @Test
    fun `Behandlingsstatus er FerdigBehandlet når det finnes 1 søknad og 1 vedtak`() {
        assertEquals(
            FerdigBehandlet,
            Behandlingsstatus(antallSøknader = 1, antallVedtak = 1).antattStatus,
        )
    }

    @Test
    fun `Behandlingsstatus er null når det finnes 0 søknader og 1 vedtak`() {
        assertEquals(
            null,
            Behandlingsstatus(antallSøknader = 0, antallVedtak = 1).antattStatus,
        )
    }

    @Test
    fun `Behandlingsstatus er FerdigBehandlet når det finnes 1 søknad og 2 vedtak`() {
        assertEquals(
            FerdigBehandlet,
            Behandlingsstatus(antallSøknader = 1, antallVedtak = 2).antattStatus,
        )
    }

    @Test
    fun `Behandlingsstatus er Ukjent når det finnes 2 søknader og 1 vedtak`() {
        assertEquals(
            Ukjent,
            Behandlingsstatus(antallSøknader = 2, antallVedtak = 1).antattStatus,
        )
    }

    @Test
    fun `Behandlingsstatus er FerdigBehandlet når det finnes 2 søknader og 2 vedtak`() {
        assertEquals(
            FerdigBehandlet, // TODO: Er dette riktig?
            Behandlingsstatus(antallVedtak = 2, antallSøknader = 2).antattStatus,
        )
    }
}
