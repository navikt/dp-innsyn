package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.serde.EttersendingVisitor

class Ettersending(
    val ettersendingId: String?,
    val søknadId: String?,
    val journalpostId: String,
    val kanal: Kanal,
    vedlegg: List<Vedlegg>
) : Innsending(vedlegg) {
    fun accept(visitor: EttersendingVisitor) {
        visitor.visitEttersending(søknadId, kanal)
    }
}
