package no.nav.dagpenger.innsyn.modell

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HendelseTest {

    @Test
    fun `en hendelse av typen søknad skal opprette en søknad`() {
        Person("ident").also { person ->
            person.håndter(SøknadHendelse("id", "ied"))

            assertTrue(person.harSøknadUnderBehandling())
        }
    }

    @Test
    fun `en hendelse av typen søknad skal gi et innslag i tidslinjen`() {
        Person("ident").also { person ->
            person.håndter(SøknadHendelse("id", "id"))

            assertTrue(person.tidslinje.hendelser.first() is SøknadHendelse)
        }
    }

    @Test
    fun `en hendelse av typen vedtak skal gi et innslag i tidslinjen`() {
        Person("ident").also { person ->
            person.håndter(VedtakHendelse("id", "HEMMELIG", "Vedtak.Status.INNVILGET"))

            assertTrue(person.tidslinje.hendelser.last() is VedtakHendelse)
        }
    }

    /*@Test
    fun `en hendelse av typen ettersending skal gi et innslag i tidslinjen`() {
        Person("ident").also { person ->
            person.håndter(EttersendingHendelse("id", Vedlegg("ident", LocalDate.now())))

            assertTrue(person.tidslinje.hendelser.any() is EttersendingHendelse)
        }
    }*/
}
