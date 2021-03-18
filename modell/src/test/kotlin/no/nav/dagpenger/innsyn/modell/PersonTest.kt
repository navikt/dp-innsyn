package no.nav.dagpenger.innsyn.modell

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PersonTest {
    @Test
    fun `Person får søknad under behandling etter ny søknad`() {
        Person("ident").also { person ->
            person.håndter(søknad("id"))

            assertTrue(person.harSøknadUnderBehandling())
        }
    }

    /*
    1..1 Søknad
        Går vi til henvendelse og får vi liste med innsendte vedlegg og manglende vedlegg
    0..* Innsendinger
        1..* Dokument (hvor ett kan være søknad)
    0..* Ettersending
    1..1 Vedtak
     */

    @Test
    fun `Person skal ikke ha noen søknad under behandling etter vedtak`() {
        Person("ident").also { person ->
            person.håndter(søknad("id1"))
            assertTrue(person.harSøknadUnderBehandling())

            person.håndter(vedtak("id1"))
            assertFalse(person.harSøknadUnderBehandling())

            person.håndter(søknad("id2"))
            person.håndter(søknad("id3"))
            assertTrue(person.harSøknadUnderBehandling())

            person.håndter(vedtak("id2"))
            assertTrue(person.harSøknadUnderBehandling())

            person.håndter(vedtak("id4"))
            assertTrue(person.harSøknadUnderBehandling())

            person.håndter(vedtak("id3"))
            assertFalse(person.harSøknadUnderBehandling())
        }
    }

    private fun vedtak(søknadId: String) = Vedtak("id", søknadId)
    private fun søknad(id: String) = Søknad(id)
    private fun søknad(vedlegg: List<String>) = Søknad("id", vedlegg.map { Vedlegg(it) }, emptyList())
}
