package no.nav.dagpenger.innsyn.mapper

import no.nav.dagpenger.innsyn.api.models.VedtakResponse
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class VedtakMapperTest {
    @Test
    fun `map vedtak`() {
        val vedtak =
            VedtakMapper(
                Vedtak(
                    vedtakId = "123",
                    fagsakId = "456",
                    status = Vedtak.Status.INNVILGET,
                    datoFattet = LocalDateTime.now(),
                    fraDato = LocalDateTime.now(),
                    tilDato = LocalDateTime.now(),
                ),
            ).response

        with(vedtak) {
            assertEquals("123", vedtakId)
            assertEquals("456", fagsakId)
            assertEquals(VedtakResponse.Status.INNVILGET, status)
            assertNotNull(datoFattet)
            assertNotNull(fraDato)
            assertNotNull(tilDato)
        }
    }
}
