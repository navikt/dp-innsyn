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
    fun `Vi mangler 2 av 4 vedlegg for å kunne behandle søknaden din`() {
        val hendelse = HendelseA(
            suspensjon = LocalDateTime.now().plusDays(14),
            components = listOf(
                HendelseA().also { it.fullfør() },
                HendelseA().also { it.fullfør() },
                HendelseA(),
                HendelseA()
            )
        )
        assertEquals(hendelse.status(), PÅBEGYNT)
        assertTrue(LocalDateTime.now().isBefore(hendelse.suspensjon))
        assertEquals(hendelse.components.size, 4)
        assertEquals(hendelse.components.filter { it.status() == FERDIG }.size, 2)
        assertEquals(hendelse.components.filter { it.status() == PLANLAGT }.size, 2)
    }

    @Test
    fun `Når saken er ferdig behandlet vil du få varsel på SMS`() {
        val hendelse = HendelseA(
            suspensjon = LocalDateTime.now().plusDays(14)
        )
        assertEquals(hendelse.status(), PLANLAGT)
        assertTrue(LocalDateTime.now().isBefore(hendelse.suspensjon))
    }
}

class HendelseA(
    var fullført: LocalDateTime? = null,
    var status: Status = PLANLAGT,
    val suspensjon: LocalDateTime? = null,
    val components: List<HendelseA> = emptyList()
) {
    fun status(): Status {
        if (components.isNotEmpty()) {
            return if (components.any { it.status() == FERDIG })
                PÅBEGYNT
            else
                PLANLAGT
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
