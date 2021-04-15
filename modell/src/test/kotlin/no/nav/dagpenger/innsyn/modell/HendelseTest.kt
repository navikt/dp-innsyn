package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.HendelseA.Status.FERDIG
import no.nav.dagpenger.innsyn.modell.HendelseA.Status.PLANLAGT
import no.nav.dagpenger.innsyn.modell.HendelseA.Status.PÅBEGYNT
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

    @Test
    fun `sortere hendelser`() {
        val h1 = HendelseA(status = PLANLAGT, frist = LocalDateTime.now().plusDays(14).withNano(0))
        val h2 = HendelseA(status = FERDIG, fullført = LocalDateTime.now().minusDays(2))
        val h3 = HendelseA(status = PÅBEGYNT, frist = LocalDateTime.now().plusDays(14).withNano(0))
        val h4 = HendelseA(status = PLANLAGT, frist = LocalDateTime.now().plusDays(10).withNano(0))
        val h5 = HendelseA(status = PÅBEGYNT, frist = LocalDateTime.now().plusDays(10).withNano(0))

        val hendelser = listOf(h1, h2, h3, h4, h5)
        assertEquals(listOf(h2, h5, h4, h3, h1), hendelser.sorted())
    }
}

class HendelseA(
    var fullført: LocalDateTime? = null,
    var status: Status = PLANLAGT,
    val frist: LocalDateTime? = null,
    val delHendelser: List<HendelseA> = emptyList()
) : Comparable<HendelseA> {
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

    override fun compareTo(other: HendelseA) = when (sorteringsDato().compareTo(other.sorteringsDato())) {
        0 -> when {
            status() == FERDIG && other.status() == PLANLAGT -> 1
            status() == FERDIG && other.status() == PÅBEGYNT -> 1
            else -> -1
        }
        else -> sorteringsDato().compareTo(other.sorteringsDato())
    }

    private fun sorteringsDato() = when (status()) {
        PLANLAGT -> frist!!
        PÅBEGYNT -> frist!!
        FERDIG -> fullført!!
    }
}
