package no.nav.dagpenger.innsyn.mapper

import no.nav.dagpenger.innsyn.api.models.VedtakResponse
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.serde.VedtakVisitor
import java.time.LocalDateTime

class VedtakMapper(val vedtak: Vedtak) : VedtakVisitor {
    private lateinit var vedtakId: String
    private lateinit var fagsakId: String
    private lateinit var status: Vedtak.Status
    private lateinit var datoFattet: LocalDateTime
    private lateinit var fraDato: LocalDateTime
    private var tilDato: LocalDateTime? = null

    init {
        vedtak.accept(this)
    }

    val response: VedtakResponse get() =
        VedtakResponse(
            vedtakId = vedtakId,
            fagsakId = fagsakId,
            status = VedtakResponse.Status.valueOf(status.name),
            datoFattet = datoFattet,
            fraDato = fraDato,
            tilDato = tilDato,
        )

    override fun visitVedtak(
        vedtakId: String,
        fagsakId: String,
        status: Vedtak.Status,
        datoFattet: LocalDateTime,
        fraDato: LocalDateTime,
        tilDato: LocalDateTime?,
    ) {
        this.vedtakId = vedtakId
        this.fagsakId = fagsakId
        this.status = status
        this.datoFattet = datoFattet
        this.fraDato = fraDato
        this.tilDato = tilDato
    }
}
