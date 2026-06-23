package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.serde.SøknadVisitor

abstract class Innsending(
    var vedlegg: List<Vedlegg>,
) {
    class Vedlegg(
        val skjemaNummer: String,
        val navn: String,
        val status: Status,
    ) {
        fun accept(visitor: SøknadVisitor) {
            visitor.visitVedlegg(skjemaNummer, navn, status)
        }

        enum class Status {
            LastetOpp,
            VedleggSendesAvAndre,
            VedleggSendesIkke,
            SendesSenere,
            SendesIkke,
            VedleggAlleredeSendt,
        }
    }
}
