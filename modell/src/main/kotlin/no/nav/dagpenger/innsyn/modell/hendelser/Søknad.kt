package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.serde.SøknadVisitor

class Søknad(
    internal val søknadId: String?,
    internal val journalpostId: String,
    internal val skjemaKode: String?,
    internal val søknadsType: SøknadsType,
    internal val kanal: Kanal
) {
    fun accept(visitor: SøknadVisitor) {
        visitor.visitSøknad(søknadId, journalpostId, skjemaKode, søknadsType, kanal)
    }

    enum class SøknadsType {
        NySøknad,
        Gjenopptak
    }
}

