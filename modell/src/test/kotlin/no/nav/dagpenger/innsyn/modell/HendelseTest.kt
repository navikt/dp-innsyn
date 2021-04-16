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
        val planlagtHendelse = HendelseA.planlagt(HendelseType.Søknad)
        val fullførtHendelse = HendelseA.ferdig(HendelseType.Søknad)
        planlagtHendelse.fullfør(fullførtHendelse)

        assertEquals(planlagtHendelse.status(), FERDIG)
        assertTrue(LocalDateTime.now().plusDays(1).isAfter(planlagtHendelse.fullført()))
    }

    @Test
    fun `Når man sender inn de to siste vedleggene blir hele dokumentasjon-hendelsen FERDIG`() {
        val vedleggA = HendelseA.ferdig(HendelseType.Ettersending)
        val vedleggB = HendelseA.ferdig(HendelseType.Ettersending)
        val vedleggC = HendelseA.planlagt(HendelseType.Ettersending)
        val vedleggD = HendelseA.planlagt(HendelseType.Ettersending)
        val dokumentasjon = HendelseA.planlagt(
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

        vedleggC.fullfør(HendelseA.ferdig(HendelseType.Ettersending))
        vedleggD.fullfør(HendelseA.ferdig(HendelseType.Ettersending))

        assertEquals(4, dokumentasjon.delHendelser.filter { it.status() == FERDIG }.size)
        assertEquals(0, dokumentasjon.delHendelser.filter { it.status() == PLANLAGT }.size)
        assertEquals(FERDIG, dokumentasjon.status())
    }

    @Test
    fun `sortere hendelser`() {
        val h1 = HendelseA.planlagt(HendelseType.Vedtak, frist = LocalDateTime.now().plusDays(14).withNano(0))
        val h2 = HendelseA.ferdig(HendelseType.Vedtak)
        // val h3 = HendelseA.påbegynt(frist = LocalDateTime.now().plusDays(14).withNano(0))
        val h4 = HendelseA.planlagt(HendelseType.Vedtak, frist = LocalDateTime.now().plusDays(10).withNano(0))
        // val h5 = HendelseA.påbegynt(frist = LocalDateTime.now().plusDays(10).withNano(0))
        // val hendelser = listOf(h1, h2, h3, h4, h5)
        // assertEquals(listOf(h2, h5, h4, h3, h1), hendelser.sorted())
        val hendelser = listOf(h1, h2, h4)
        assertEquals(listOf(h2, h4, h1), hendelser.sorted())
    }

    @Test
    fun mangelbrev() {
        val søknad = HendelseA.ferdig(type = HendelseType.Søknad)
        val planlagtVedtak = HendelseA.planlagt(type = HendelseType.Vedtak)
        val hendelser = mutableListOf(søknad, planlagtVedtak)
        // Saksbehandler finner mangler og sender mangelbrev
        val mangelbrev = HendelseA.ferdig(type = HendelseType.Mangelbrev)
        val forventetEttersending = HendelseA.planlagt(type = HendelseType.Ettersending)

        hendelser.add(mangelbrev)
        hendelser.add(forventetEttersending)
        // Søker ettersender
        val ettersending = HendelseA.ferdig(type = HendelseType.Ettersending)
        hendelser.findLast {
            it.type == HendelseType.Ettersending
        }?.fullfør(ettersending)

        assertEquals(4, hendelser.size)
        assertEquals(ettersending, forventetEttersending.løstAv)
        // Søker får vedtak
        val vedtak = HendelseA.ferdig(type = HendelseType.Vedtak)
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

class HendelseA private constructor(
    val type: HendelseType,
    private val fullført: LocalDateTime? = null,
    val frist: LocalDateTime? = null,
    val delHendelser: List<HendelseA> = emptyList(),
    var løstAv: HendelseA? = null,
    private var tilstand: Tilstand = Planlagt
) : Comparable<HendelseA> {
    companion object {
        fun planlagt(type: HendelseType, frist: LocalDateTime? = null, delHendelser: List<HendelseA> = emptyList()) =
            HendelseA(
                type = type,
                frist = frist,
                delHendelser = delHendelser
            )

        fun ferdig(type: HendelseType) = HendelseA(
            type = type,
            fullført = LocalDateTime.now(),
            tilstand = Fullført
        )
    }

    fun status() = tilstand.status(this)
    fun fullført() = tilstand.fullført(this)
    fun fullfør(løstAv: HendelseA) = tilstand.fullfør(this, løstAv)

    enum class Status {
        PLANLAGT,
        PÅBEGYNT,
        FERDIG,
    }

    private interface Tilstand {
        fun fullført(hendelse: HendelseA): LocalDateTime?
        fun status(hendelse: HendelseA): Status
        fun fullfør(hendelse: HendelseA, løstAv: HendelseA)
    }

    private object Planlagt : Tilstand {
        override fun fullført(hendelse: HendelseA): LocalDateTime? = null

        override fun status(hendelse: HendelseA): Status {
            if (hendelse.delHendelser.isNotEmpty()) {
                return when {
                    hendelse.delHendelser.all { it.status() == FERDIG } -> FERDIG
                    hendelse.delHendelser.any { it.status() == FERDIG } -> PÅBEGYNT
                    else -> PLANLAGT
                }
            }

            return PLANLAGT
        }

        override fun fullfør(hendelse: HendelseA, løstAv: HendelseA) {
            if (løstAv.status() != FERDIG) throw IllegalStateException("Kan ikke fullføre med uferdig hendelse")
            hendelse.løstAv = løstAv
            hendelse.tilstand = Fullført
        }
    }

    private object Fullført : Tilstand {
        override fun fullført(hendelse: HendelseA): LocalDateTime? = hendelse.løstAv?.fullført ?: hendelse.fullført

        override fun status(hendelse: HendelseA) = FERDIG

        override fun fullfør(hendelse: HendelseA, løstAv: HendelseA) {
            throw IllegalStateException("Kan ikke fullføre allerede ferdig hendelse")
        }
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
        FERDIG -> fullført()!!
    }
}
