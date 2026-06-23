package no.nav.dagpenger.innsyn.mapper

import no.nav.dagpenger.innsyn.api.models.VedtakResponse
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak

object VedtakMapper {
    fun Vedtak.toResponse(): VedtakResponse =
        VedtakResponse(
            vedtakId = vedtakId,
            fagsakId = fagsakId,
            status = VedtakResponse.Status.valueOf(status.name),
            datoFattet = datoFattet,
            fraDato = fraDato,
            tilDato = tilDato,
        )
}
