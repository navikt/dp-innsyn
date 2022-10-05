package no.nav.dagpenger.innsyn.behandlingsstatus

import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.FerdigBehandlet
import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.UnderBehandling
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class BehandlingsstatusTest {

    @Test
    fun `Behandlingsstatus er null dersom det ikke finnes søknader eller vedtak`() {
        assertEquals(null, Behandlingsstatus(emptyList(), emptyList()).status)
    }

    @Test
    fun `Behandlingsstatus er UnderBehandling når det finnes 1 søknad og 0 vedtak`() {
        assertEquals(
            UnderBehandling,
            Behandlingsstatus(
                vedtak = emptyList(),
                søknader = listOf(søknad())
            ).status
        )
    }

    @Test
    fun `Behandlingsstatus er null når det finnes 0 søknader og 1 vedtak`() {
        assertEquals(
            null,
            Behandlingsstatus(
                vedtak = listOf(vedtak()),
                søknader = emptyList()
            ).status
        )
    }

    @Test
    fun `Behandlingsstatus er FerdigBehandlet når det finnes 1 søknad og 1 vedtak`() {
        assertEquals(
            FerdigBehandlet,
            Behandlingsstatus(
                vedtak = listOf(vedtak()),
                søknader = listOf(søknad())
            ).status
        )
    }

    @Test
    fun `Behandlingsstatus er FerdigBehandlet når det finnes 1 søknad og 2 vedtak`() {
        assertEquals(
            FerdigBehandlet,
            Behandlingsstatus(
                vedtak = listOf(vedtak(), vedtak()),
                søknader = listOf(søknad())
            ).status
        )
    }

    @Test
    fun `Behandlingsstatus er Ukjent når det finnes 2 søknader og 1 vedtak`() {
        assertEquals(
            Behandlingsstatus.Status.UnderOgFerdigBehandlet,
            Behandlingsstatus(
                vedtak = listOf(vedtak()),
                søknader = listOf(søknad(), søknad())
            ).status
        )
    }

    @Test
    fun `Behandlingsstatus er FerdigBehandlet når det finnes 2 søknader og 2 vedtak`() {
        assertEquals(
            FerdigBehandlet,
            Behandlingsstatus(
                vedtak = listOf(vedtak(), vedtak()),
                søknader = listOf(søknad(), søknad())
            ).status
        )
    }

    private fun søknad() = Søknad(
        søknadId = "søknadId",
        journalpostId = "journalpostId",
        skjemaKode = "NAV 04-01.03",
        søknadsType = Søknad.SøknadsType.NySøknad,
        kanal = Kanal.Digital,
        datoInnsendt = LocalDateTime.now(),
        vedlegg = listOf(Innsending.Vedlegg("123", "navn", Innsending.Vedlegg.Status.LastetOpp)),
        tittel = "tittel"
    )

    private fun vedtak() = Vedtak(
        vedtakId = "1",
        fagsakId = "1",
        status = Vedtak.Status.INNVILGET,
        datoFattet = LocalDateTime.now(),
        fraDato = LocalDateTime.now(),
        tilDato = LocalDateTime.now(),
    )
}