package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.serde.VedtakVisitor
import java.time.LocalDateTime

class Vedtak constructor(
    val vedtakId: String,
    val fagsakId: String,
    val status: Status,
    val datoFattet: LocalDateTime,
    val fraDato: LocalDateTime,
    val tilDato: LocalDateTime?,
) {
    fun accept(visitor: VedtakVisitor) {
        visitor.visitVedtak(vedtakId, fagsakId, status, datoFattet, fraDato, tilDato)
    }

    enum class Status {
        INNVILGET,
        AVSLÅTT,
        STANS,
        ENDRING,
    }
}
