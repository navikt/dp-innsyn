package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.serde.SøknadVisitor
import java.time.LocalDateTime

class Søknad(
    private val søknadId: String?,
    private val journalpostId: String,
    private val skjemaKode: String?,
    private val søknadsType: SøknadsType,
    private val kanal: Kanal,
    private val datoInnsendt: LocalDateTime
) {
    companion object {
        fun List<Søknad>.har(søknad: Søknad) = this.any { it.journalpostId == søknad.journalpostId }
    }

    fun accept(visitor: SøknadVisitor) {
        visitor.visitSøknad(søknadId, journalpostId, skjemaKode, søknadsType, kanal, datoInnsendt)
    }

    enum class SøknadsType {
        NySøknad,
        Gjenopptak
    }
}
