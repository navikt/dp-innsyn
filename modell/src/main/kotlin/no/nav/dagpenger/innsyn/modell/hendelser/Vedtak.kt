package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.serde.VedtakVisitor

class Vedtak constructor(
    private val vedtakId: String,
    val fagsakId: String,
    val status: Status,
) {
    fun accept(visitor: VedtakVisitor) {
        visitor.visitVedtak(vedtakId, fagsakId, status)
    }

    enum class Status {
        INNVILGET,
        AVSLÅTT,
        STANS,
        ENDRING
    }
}
