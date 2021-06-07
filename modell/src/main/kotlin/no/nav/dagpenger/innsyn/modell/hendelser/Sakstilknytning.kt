package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.serde.SakstilknytningVisitor

class Sakstilknytning constructor(
    internal val journalpostId: String,
    internal val fagsakId: String,
) {
    fun accept(visitor: SakstilknytningVisitor) {
        visitor.visitSakstilknytning(journalpostId, fagsakId)
    }
}
