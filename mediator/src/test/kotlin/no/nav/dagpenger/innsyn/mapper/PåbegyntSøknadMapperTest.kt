package no.nav.dagpenger.innsyn.mapper

import no.nav.dagpenger.innsyn.tjenester.PåbegyntSøknadDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.util.UUID

class PåbegyntSøknadMapperTest {
    @Test
    fun `map nySøknadsdialog påbegynte søknad`() {
        val uuid = UUID.randomUUID()
        val påbegyntSøknadMapper =
            PåbegyntSøknadMapper(
                dto =
                    PåbegyntSøknadDto(
                        uuid = uuid,
                        opprettet = ZonedDateTime.now(),
                        sistEndret = ZonedDateTime.now(),
                    ),
                erNySøknadsdialog = true,
            )

        val response = påbegyntSøknadMapper.response

        with(response) {
            assertEquals("Søknad om dagpenger", tittel)
            assertEquals(uuid.toString(), søknadId)
            assertEquals(uuid.toString(), behandlingsId)
            assertNotNull(sistEndret)
            assertNotNull(erNySøknadsdialog)
            assertTrue(erNySøknadsdialog!!)
            assertEquals("https://arbeid.intern.dev.nav.no/dagpenger/dialog/soknad/$uuid", endreLenke)
        }
    }

    @Test
    fun `map gammelSøknadsdialog påbegynte søknad`() {
        val uuid = UUID.randomUUID()
        val påbegyntSøknadMapper =
            PåbegyntSøknadMapper(
                dto =
                    PåbegyntSøknadDto(
                        uuid = uuid,
                        opprettet = ZonedDateTime.now(),
                        sistEndret = ZonedDateTime.now(),
                    ),
                erNySøknadsdialog = false,
            )

        val response = påbegyntSøknadMapper.response

        with(response) {
            assertEquals("Søknad om dagpenger", tittel)
            assertEquals(uuid.toString(), søknadId)
            assertEquals(uuid.toString(), behandlingsId)
            assertNotNull(sistEndret)
            assertNotNull(erNySøknadsdialog)
            assertTrue(!erNySøknadsdialog!!)
            assertEquals("https://tjenester.nav.no/soknaddagpenger-innsending/soknad/$uuid", endreLenke)
        }
    }
}
