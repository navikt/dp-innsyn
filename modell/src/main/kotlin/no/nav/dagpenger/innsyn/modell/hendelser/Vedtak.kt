package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.serde.VedtakVisitor
import java.time.LocalDateTime

class Vedtak constructor(
    private val vedtakId: String,
    private val fagsakId: String,
    private val status: Status,
    private val datoFattet: LocalDateTime
) {
    fun accept(visitor: VedtakVisitor) {
        visitor.visitVedtak(vedtakId, fagsakId, status, datoFattet)
    }

    enum class Status {
        INNVILGET,
        AVSLÅTT,
        STANS,
        ENDRING
    }
}
