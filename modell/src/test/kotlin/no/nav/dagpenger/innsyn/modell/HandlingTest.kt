package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.Handling.Status.FERDIG
import no.nav.dagpenger.innsyn.modell.Handling.Status.PLANLAGT
import no.nav.dagpenger.innsyn.modell.Handling.Status.PÅBEGYNT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class HandlingTest {
    @Test
    fun `NAV har mottatt din søknad om dagpenger`() {
        val planlagtHendelse = Handling.planlagt(HendelseType.Søknad)
        val fullførtHendelse = Handling.ferdig(HendelseType.Søknad)
        planlagtHendelse.fullfør(fullførtHendelse)

        assertEquals(planlagtHendelse.status(), FERDIG)
        assertTrue(LocalDateTime.now().plusDays(1).isAfter(planlagtHendelse.fullført()))
    }

    @Test
    fun `Når man sender inn de to siste vedleggene blir hele dokumentasjon-hendelsen FERDIG`() {
        val vedleggA = Handling.ferdig(HendelseType.Ettersending)
        val vedleggB = Handling.ferdig(HendelseType.Ettersending)
        val vedleggC = Handling.planlagt(HendelseType.Ettersending)
        val vedleggD = Handling.planlagt(HendelseType.Ettersending)
        val dokumentasjon = Handling.planlagt(
            type = HendelseType.Ettersending,
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

        vedleggC.fullfør(Handling.ferdig(HendelseType.Ettersending))
        vedleggD.fullfør(Handling.ferdig(HendelseType.Ettersending))

        assertEquals(4, dokumentasjon.delHendelser.filter { it.status() == FERDIG }.size)
        assertEquals(0, dokumentasjon.delHendelser.filter { it.status() == PLANLAGT }.size)
        assertEquals(FERDIG, dokumentasjon.status())
    }

    @Test
    fun `sortere hendelser`() {
        val h1 = Handling.planlagt(HendelseType.Vedtak, frist = LocalDateTime.now().plusDays(14).withNano(0))
        val h2 = Handling.ferdig(HendelseType.Vedtak)
        // val h3 = HendelseA.påbegynt(frist = LocalDateTime.now().plusDays(14).withNano(0))
        val h4 = Handling.planlagt(HendelseType.Vedtak, frist = LocalDateTime.now().plusDays(10).withNano(0))
        // val h5 = HendelseA.påbegynt(frist = LocalDateTime.now().plusDays(10).withNano(0))
        // val hendelser = listOf(h1, h2, h3, h4, h5)
        // assertEquals(listOf(h2, h5, h4, h3, h1), hendelser.sorted())
        val hendelser = listOf(h1, h2, h4)
        assertEquals(listOf(h2, h4, h1), hendelser.sorted())
    }

    @Test
    fun mangelbrev() {
        val søknad = Handling.ferdig(type = HendelseType.Søknad)
        val planlagtVedtak = Handling.planlagt(type = HendelseType.Vedtak)
        val hendelser = mutableListOf(søknad, planlagtVedtak)
        // Saksbehandler finner mangler og sender mangelbrev
        val mangelbrev = Handling.ferdig(type = HendelseType.Mangelbrev)
        val forventetEttersending = Handling.planlagt(type = HendelseType.Ettersending)

        hendelser.add(mangelbrev)
        hendelser.add(forventetEttersending)
        // Søker ettersender
        val ettersending = Handling.ferdig(type = HendelseType.Ettersending)
        hendelser.findLast {
            it.type == HendelseType.Ettersending
        }?.fullfør(ettersending)

        assertEquals(4, hendelser.size)
        assertEquals(ettersending, forventetEttersending.løstAv)
        // Søker får vedtak
        val vedtak = Handling.ferdig(type = HendelseType.Vedtak)
        hendelser.findLast {
            it.type == HendelseType.Vedtak
        }?.fullfør(vedtak)

        assertEquals(4, hendelser.size)
        assertEquals(vedtak, planlagtVedtak.løstAv)
    }
}

enum class HendelseType {
    Søknad,
    Vedtak,
    Ettersending,
    Mangelbrev
}

class Handling private constructor(
    internal val type: HendelseType,
    private val fullført: LocalDateTime? = null,
    internal val frist: LocalDateTime? = null,
    internal val delHendelser: List<Handling> = emptyList(),
    internal var løstAv: Handling? = null,
    private var tilstand: Tilstand = Planlagt
) : Comparable<Handling> {
    companion object {
        fun planlagt(type: HendelseType, frist: LocalDateTime? = null, delHendelser: List<Handling> = emptyList()) =
            Handling(
                type = type,
                frist = frist,
                delHendelser = delHendelser
            )

        fun ferdig(type: HendelseType) = Handling(
            type = type,
            fullført = LocalDateTime.now(),
            tilstand = Fullført
        )
    }

    fun status() = tilstand.status(this)
    fun fullført() = tilstand.fullført(this)
    fun fullfør(løstAv: Handling) = tilstand.fullfør(this, løstAv)

    enum class Status {
        PLANLAGT,
        PÅBEGYNT,
        FERDIG,
    }

    private interface Tilstand {
        fun fullført(hendelse: Handling): LocalDateTime?
        fun status(hendelse: Handling): Status
        fun fullfør(hendelse: Handling, løstAv: Handling)
    }

    private object Planlagt : Tilstand {
        override fun fullført(hendelse: Handling): LocalDateTime? = null

        override fun status(hendelse: Handling): Status {
            if (hendelse.delHendelser.isNotEmpty()) {
                return when {
                    hendelse.delHendelser.all { it.status() == FERDIG } -> FERDIG
                    hendelse.delHendelser.any { it.status() == FERDIG } -> PÅBEGYNT
                    else -> PLANLAGT
                }
            }

            return PLANLAGT
        }

        override fun fullfør(hendelse: Handling, løstAv: Handling) {
            if (løstAv.status() != FERDIG) throw IllegalStateException("Kan ikke fullføre med uferdig hendelse")
            hendelse.løstAv = løstAv
            hendelse.tilstand = Fullført
        }
    }

    private object Fullført : Tilstand {
        override fun fullført(hendelse: Handling): LocalDateTime? = hendelse.løstAv?.fullført ?: hendelse.fullført

        override fun status(hendelse: Handling) = FERDIG

        override fun fullfør(hendelse: Handling, løstAv: Handling) {
            throw IllegalStateException("Kan ikke fullføre allerede ferdig hendelse")
        }
    }

    override fun compareTo(other: Handling) = when (sorteringsDato().compareTo(other.sorteringsDato())) {
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
        FERDIG -> fullført()!!
    }
}
