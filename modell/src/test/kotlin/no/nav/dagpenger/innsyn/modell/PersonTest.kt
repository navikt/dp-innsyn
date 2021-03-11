package no.nav.dagpenger.innsyn.modell

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PersonTest {
    @Test
    fun `Person får søknad under behandling etter ny søknad`() {
        Person("ident").also { person ->
            person.håndter(søknadHendelse())

            assertTrue(person.harSøknadUnderBehandling())
        }
    }

    @Test
    fun `Person skal ikke ha noen søknad under behandling etter vedtak`() {
        Person("ident").also { person ->
            person.håndter(søknadHendelse())
            assertTrue(person.harSøknadUnderBehandling())

            person.håndter(vedtakHendelse())
            assertFalse(person.harSøknadUnderBehandling())

            person.håndter(søknadHendelse())
            person.håndter(søknadHendelse())
            assertTrue(person.harSøknadUnderBehandling())

            person.håndter(vedtakHendelse())
            assertFalse(person.harSøknadUnderBehandling())
        }
    }

    private fun vedtakHendelse() = VedtakHendelse("id", "vedtakId", "Innvilget")
    private fun søknadHendelse() = SøknadHendelse("id", "id")
/*
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
