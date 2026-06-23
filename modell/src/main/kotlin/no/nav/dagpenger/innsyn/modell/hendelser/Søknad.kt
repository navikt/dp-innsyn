package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.serde.SøknadVisitor
import java.time.LocalDateTime

class Søknad(
    val søknadId: String?,
    val journalpostId: String,
    val skjemaKode: String?,
    val søknadsType: SøknadsType,
    val kanal: Kanal,
    val datoInnsendt: LocalDateTime,
    vedlegg: List<Vedlegg>,
    val tittel: String?,
) : Innsending(vedlegg) {
    init {
        check(papirKanIkkeHaVedlegg) { "Søknader uten søknadId (papir) kan ikke ha vedlegg" }
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
        Gjenopptak,
    }

    private val papirKanIkkeHaVedlegg get() = !(søknadId == null && vedlegg.isNotEmpty())
}
