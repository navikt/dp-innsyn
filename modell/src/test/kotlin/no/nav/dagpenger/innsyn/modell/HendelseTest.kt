package no.nav.dagpenger.innsyn.modell

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HendelseTest {

    @Test
    fun `en hendelse av typen søknad skal opprette en søknad`() {
        Person("ident").also { person ->
            person.håndter(SøknadHendelse("id"))

            assertTrue(person.harSøknadUnderBehandling())
        }
    }

    @Test
    fun `en hendelse av typen søknad skal gi et innslag i tidslinjen`() {
        Person("ident").also { person ->
            person.håndter(SøknadHendelse("id"))

            assertTrue(person.tidslinje.hendelser.first() is SøknadHendelse)
        }
    }

    @Test
    fun `en hendelse av typen vedtak skal gi et innslag i tidslinjen`() {
        Person("ident").also { person ->
            person.håndter(VedtakHendelse("id", "HEMMELIG"))

            assertTrue(person.tidslinje.hendelser.last() is VedtakHendelse)
        }
    }
}
