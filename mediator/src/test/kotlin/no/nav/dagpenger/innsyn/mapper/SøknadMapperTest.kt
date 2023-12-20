package no.nav.dagpenger.innsyn.mapper

import no.nav.dagpenger.innsyn.api.models.SoknadResponse
import no.nav.dagpenger.innsyn.api.models.VedleggResponse
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.tjenester.Lenker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class SøknadMapperTest {
    @Test
    fun `map søknad med SøknadId`() {
        val søknadId = UUID.randomUUID().toString()
        val datoInnsendt = LocalDateTime.now()

        val søknad =
            SøknadMapper(
                Søknad(
                    søknadId = søknadId,
                    journalpostId = "456",
                    skjemaKode = "789",
                    søknadsType = Søknad.SøknadsType.NySøknad,
                    kanal = Kanal.Digital,
                    datoInnsendt = datoInnsendt,
                    tittel = "tittel",
                    vedlegg =
                        listOf(
                            Innsending.Vedlegg(
                                skjemaNummer = "skjemaNummer",
                                navn = "navn",
                                status = Innsending.Vedlegg.Status.LastetOpp,
                            ),
                        ),
                ),
            ).response

        with(søknad) {
            assertEquals(søknadId, søknadId)
            assertEquals("456", journalpostId)
            assertEquals("789", skjemaKode)
            assertEquals(datoInnsendt, datoInnsendt)
            assertEquals(SoknadResponse.SøknadsType.NySøknad, søknadsType)
            assertNotNull(erNySøknadsdialog)
            assertTrue(erNySøknadsdialog!!)
            assertEquals(Lenker.ettersendelseNySøknadsdialog(søknadId), endreLenke)
            assertEquals(SoknadResponse.Kanal.Digital, kanal)
            assertEquals("tittel", tittel)
            assertEquals(1, vedlegg?.size)
            with(vedlegg!![0]) {
                assertEquals("skjemaNummer", skjemaNummer)
                assertEquals("navn", navn)
                assertEquals(VedleggResponse.Status.LastetOpp, status)
            }
        }
    }

    @Test
    fun `map søknad uten SøknadId`() {
        val datoInnsendt = LocalDateTime.now()

        val søknad =
            SøknadMapper(
                Søknad(
                    søknadId = null,
                    journalpostId = "journalpostId",
                    skjemaKode = "skjemaKode",
                    søknadsType = Søknad.SøknadsType.NySøknad,
                    kanal = Kanal.Digital,
                    datoInnsendt = datoInnsendt,
                    tittel = "tittel",
                    vedlegg = emptyList(),
                ),
            ).response

        with(søknad) {
            assertNull(søknadId)
            assertNull(erNySøknadsdialog)
            assertNull(endreLenke)
            assertEquals("journalpostId", journalpostId)
            assertEquals("skjemaKode", skjemaKode)
            assertEquals(SoknadResponse.SøknadsType.NySøknad, søknadsType)
            assertEquals(SoknadResponse.Kanal.Digital, kanal)
            assertEquals("tittel", tittel)
            assertEquals(0, vedlegg?.size)
        }
    }
}
