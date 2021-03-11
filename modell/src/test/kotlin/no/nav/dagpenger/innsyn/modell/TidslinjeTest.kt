package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.Tidslinje.TidslinjeObservatør
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TidslinjeTest {
    @Test
    fun `en hendelse skal inn på tidslinjen`() {
        Tidslinje().also { tidslinje ->
            val hendelse = SøknadHendelse("id", "ied")
            tidslinje.leggTil(hendelse)

            assertTrue(tidslinje.all { it == hendelse })
        }
    }

    @Test
    fun `man kan lytte på nye hendelser i tidslinjen`() {
        Tidslinje().also { tidslinje ->
            val søknadHendelse = SøknadHendelse("id", "ied")

            tidslinje.lytt(object : TidslinjeObservatør {
                override fun hendelse(hendelse: Hendelse) {
                    assertTrue(søknadHendelse == hendelse)
                }
            })

            søknadHendelse.also {
                tidslinje.leggTil(it)
            }
        }
    }
}
