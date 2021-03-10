package no.nav.dagpenger.innsyn.modell

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PersonTest {
    @Test
    fun `Person kan motta ny søknad`() {
        Person("ident").also { person ->
            person.håndter(SøknadHendelse("id"))

            assertTrue(person.harSøknadUnderBehandling())
        }
    }

    @Test
    fun `Person kan få vedtak`() {
        Person("ident").also { person ->
            person.håndter(VedtakHendelse("vedtakId", "søknadId"))

            assertFalse(person.harSøknadUnderBehandling())
        }
    }
}
