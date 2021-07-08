package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.serde.SøknadVisitor

abstract class Innsending(
    internal var vedlegg: List<Vedlegg>
) {
    class Vedlegg(
        private val skjemaNummer: String,
        private val navn: String,
        private val status: Status
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
            VedleggAlleredeSendt
        }
    }
}
