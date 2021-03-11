package no.nav.dagpenger.innsyn.modell

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PersonTest {
    @Test
    fun `Person kan motta søknadshendelse`() {
        Person("ident").also { person ->
            person.håndter(SøknadHendelse("id", "id"))

            assertTrue(person.harSøknadUnderBehandling())
        }
    }

    @Test
    fun `Person motta vedtakhendelse`() {
        Person("ident").also { person ->
            person.håndter(VedtakHendelse("vedtakId", "søknadId", "INNVILGET"))

            assertFalse(person.harSøknadUnderBehandling())
        }
    }

    /*
    @Test
    fun `søknad markeres som behandlet etter vedtak`() {
        Person("ident").also { person ->
            person.håndter(Søknad("id"))
            assertTrue(person.harSøknadUnderBehandling())

            person.håndter(Vedtak("vedtakId", "søknadId", INNVILGET))
            assertFalse(person.harSøknadUnderBehandling())

            person.håndter(Søknad("id1"))
            person.håndter(Søknad("id2"))
            assertTrue(person.harSøknadUnderBehandling())

            person.håndter(Vedtak("vedtakId", "id1", INNVILGET))
            assertFalse(person.harSøknadUnderBehandling())
        }
    }

    @Test
    fun `vedlegg skal ettersendes`() {
        val søknad = Søknad("id", listOf(Vedlegg("id")))

        Person("ident").also { person ->
            person.håndter(søknad)
            assertFalse(søknad.erKomplett)
            person.håndter(Ettersending("id", listOf(Vedlegg("id"))))
            assertTrue(søknad.erKomplett)
        }
    }

    @Test
    fun `uforventede vedlegg skal kunne ettersendes`() {
        val søknad = Søknad("id")

        Person("ident").also { person ->
            person.håndter(søknad)
            person.håndter(Ettersending("id", listOf(Vedlegg("id"))))
            assertTrue(søknad.erKomplett)
        }
    }*/
}
