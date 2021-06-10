package no.nav.dagpenger.innsyn.modell.serde

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import java.time.LocalDateTime

class VedtakJsonBuilder(val vedtak: Vedtak) : VedtakVisitor {
    private val mapper = ObjectMapper()
    private val root = mapper.createObjectNode()

    init {
        vedtak.accept(this)
    }

    fun resultat() = root

    override fun visitVedtak(
        vedtakId: String,
        fagsakId: String,
        status: Vedtak.Status,
        datoFattet: LocalDateTime,
        fraDato: LocalDateTime,
        tilDato: LocalDateTime?
    ) {
        root.put("vedtakId", vedtakId)
        root.put("fagsakId", fagsakId)
        root.put("status", status.toString())
        root.put("datoFattet", datoFattet.toString())
        root.put("fraDato", fraDato.toString())
        root.put("tilDato", tilDato.toString())
    }
}
