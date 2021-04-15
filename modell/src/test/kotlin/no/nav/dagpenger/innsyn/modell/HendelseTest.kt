package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.HendelseA.Status.FERDIG
import no.nav.dagpenger.innsyn.modell.HendelseA.Status.PLANLAGT
import no.nav.dagpenger.innsyn.modell.HendelseA.Status.PÅBEGYNT
import no.nav.dagpenger.innsyn.modell.hendelser.Hendelse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class HendelseTest {
    @Test
    fun `NAV har mottatt din søknad om dagpenger`() {
        val hendelse = HendelseA()
        hendelse.fullfør()
        assertEquals(hendelse.status(), FERDIG)
        assertTrue(LocalDateTime.now().plusDays(1).isAfter(hendelse.fullført))
    }

    @Test
    fun `Vi mangler 2 av 4 vedlegg for å kunne behandle søknaden din`() {
        val dokumentasjon = HendelseA(
            frist = LocalDateTime.now().plusDays(14),
            delHendelser = listOf(
                HendelseA().also { it.fullfør() },
                HendelseA().also { it.fullfør() },
                HendelseA(),
                HendelseA()
            )
        )
        assertEquals(PÅBEGYNT, dokumentasjon.status())
        assertTrue(LocalDateTime.now().isBefore(dokumentasjon.frist))
        assertEquals(4, dokumentasjon.delHendelser.size)
        assertEquals(2, dokumentasjon.delHendelser.filter { it.status() == FERDIG }.size)
        assertEquals(2, dokumentasjon.delHendelser.filter { it.status() == PLANLAGT }.size)
    }

    @Test
    fun `Når man sender inn de to siste vedleggene blir hele dokumentasjon-hendelsen FERDIG`() {
        val vedleggA = HendelseA().also { it.fullfør() }
        val vedleggB = HendelseA().also { it.fullfør() }
        val vedleggC = HendelseA()
        val vedleggD = HendelseA()

        val dokumentasjon = HendelseA(
            frist = LocalDateTime.now().plusDays(14),
            delHendelser = listOf(
                vedleggA,
                vedleggB,
                vedleggC,
                vedleggD
            )
        )
        assertEquals(PÅBEGYNT, dokumentasjon.status())
        assertTrue(LocalDateTime.now().isBefore(dokumentasjon.frist))
        assertEquals(dokumentasjon.delHendelser.size, 4)
        assertEquals(dokumentasjon.delHendelser.filter { it.status() == FERDIG }.size, 2)
        assertEquals(dokumentasjon.delHendelser.filter { it.status() == PLANLAGT }.size, 2)

        vedleggC.fullfør()
        vedleggD.fullfør()

        assertEquals(4, dokumentasjon.delHendelser.filter { it.status() == FERDIG }.size)
        assertEquals(0, dokumentasjon.delHendelser.filter { it.status() == PLANLAGT }.size)
        assertEquals(FERDIG, dokumentasjon.status())
    }

    @Test
    fun `Når saken er ferdig behandlet vil du få varsel på SMS`() {
        val hendelse = HendelseA(
            frist = LocalDateTime.now().plusDays(14)
        )
        assertEquals(hendelse.status(), PLANLAGT)
        assertTrue(LocalDateTime.now().isBefore(hendelse.frist))
    }
}

class HendelseA(
    var fullført: LocalDateTime? = null,
    var status: Status = PLANLAGT,
    val frist: LocalDateTime? = null,
    val delHendelser: List<HendelseA> = emptyList()
) {
    fun status(): Status {
        if (delHendelser.isNotEmpty()) {
            return when {
                delHendelser.all { it.status() == FERDIG } -> FERDIG
                delHendelser.any { it.status() == FERDIG } -> PÅBEGYNT
                else -> PLANLAGT
            }
        }
        return status
    }

    fun fullfør() {
        fullført = LocalDateTime.now()
        status = FERDIG
    }

    enum class Status {
        PLANLAGT,
        PÅBEGYNT,
        FERDIG,
    }
}
