package no.nav.dagpenger.innsyn.modell

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PersonTest {
    @Test
    fun `Person får søknad under behandling etter ny søknad`() {
        Person("ident").also { person ->
            person.håndter(søknad())

            assertTrue(person.harSøknadUnderBehandling())
        }
    }

    @Test
    fun `Person skal ikke ha noen søknad under behandling etter vedtak`() {
        Person("ident").also { person ->
            person.håndter(søknad())
            assertTrue(person.harSøknadUnderBehandling())

            person.håndter(vedtak())
            assertFalse(person.harSøknadUnderBehandling())

            person.håndter(søknad())
            person.håndter(søknad())
            assertTrue(person.harSøknadUnderBehandling())

            person.håndter(vedtak())
            assertFalse(person.harSøknadUnderBehandling())
        }
    }

    /*@Test
    fun `vedlegg skal ettersendes`() {
        val søknad = søknad(listOf("1","2"))

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

    private fun vedtak() = Vedtak("id", "vedtakId")
    private fun søknad() = Søknad("id")
    private fun søknad(vedlegg: List<String>) = Søknad("id", vedlegg.map { Vedlegg(it) })
}
