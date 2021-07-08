package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.serde.SøknadVisitor
import java.time.LocalDateTime

class Søknad(
    private val søknadId: String?,
    private val journalpostId: String,
    private val skjemaKode: String?,
    private val søknadsType: SøknadsType,
    private val kanal: Kanal,
    private val datoInnsendt: LocalDateTime,
    vedlegg: List<Vedlegg>,
    private val tittel: String?,
) : Innsending(vedlegg) {
    init {
        check(søknadId == null && vedlegg.isEmpty()) { "Søknader uten søknadId kan ikke ha vedlegg" }
    }

    companion object {
        fun List<Søknad>.har(søknad: Søknad) = this.any { it.journalpostId == søknad.journalpostId }
    }

    fun accept(visitor: SøknadVisitor) {
        visitor.visitSøknad(søknadId, journalpostId, skjemaKode, søknadsType, kanal, datoInnsendt, tittel)
        vedlegg.forEach { it.accept(visitor) }
    }

    fun håndter(ettersending: Ettersending) {
        if (søknadId != ettersending.søknadId) return
        vedlegg = ettersending.vedlegg
    }

    enum class SøknadsType {
        NySøknad,
        Gjenopptak
    }
}
