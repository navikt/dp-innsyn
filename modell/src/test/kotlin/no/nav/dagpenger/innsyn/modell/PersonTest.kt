package no.nav.dagpenger.innsyn.modell

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PersonTest {
    @Test
    fun `Person kan motta ny søknad`() {
        Person("ident").also { person ->
            person.håndter(Søknad("id"))

            assertTrue(person.harSøknadUnderBehandling())
        }
    }

    @Test
    fun `Person kan få vedtak`() {
        Person("ident").also { person ->
            person.håndter(Vedtak("vedtakId", "søknadId"))

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
}

