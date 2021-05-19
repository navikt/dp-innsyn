package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Journalføring
import no.nav.dagpenger.innsyn.modell.hendelser.Mangelbrev
import no.nav.dagpenger.innsyn.modell.hendelser.PapirSøknad
import no.nav.dagpenger.innsyn.modell.hendelser.Saksbehandling
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.serde.ProsessIdVisitor
import java.util.UUID

data class EksternId(
    val type: String,
    val id: String
)

class ProsessId(
    internal val internId: UUID,
    private val eksterneIder: MutableList<EksternId>
) {
    constructor() : this(UUID.randomUUID(), mutableListOf())

    fun håndter(søknad: Søknad): Boolean {
        if (eksterneIder.any { it.type == "søknad" }) return false
        eksterneIder.add(søknadId(søknad.søknadId))
        eksterneIder.add(journalpostId(søknad.journalpostId))
        return true
    }

    fun håndter(papirSøknad: PapirSøknad): Boolean {
        eksterneIder.add(papirSøknadId(papirSøknad.journalpostId))
        eksterneIder.add(journalpostId(papirSøknad.journalpostId))
        return true
    }

    fun håndter(journalføring: Journalføring): Boolean {
        if (!eksterneIder.contains(journalpostId(journalføring.journalpostId))) return false
        eksterneIder.add(fagsakId(journalføring.fagsakId))
        return true
    }

    fun håndter(ettersending: Ettersending): Boolean {
        if (eksterneIder.contains(søknadId(ettersending.søknadId))) return true
        return false
    }

    fun håndter(vedtak: Vedtak): Boolean {
        if (eksterneIder.contains(fagsakId(vedtak.fagsakId))) return true
        return false
    }

    fun håndter(mangelbrev: Mangelbrev): Boolean {
        return true
    }

    fun håndter(saksbehandling: Saksbehandling): Boolean {
        return true
    }

    private fun søknadId(søknadId: String) = EksternId("søknad", søknadId)

    private fun papirSøknadId(papirSøknadId: String) = EksternId("papirSøknadId", papirSøknadId)

    private fun journalpostId(journalpostId: String) = EksternId("journalpostId", journalpostId)

    private fun fagsakId(fagsakId: String) = EksternId("fagsakid", fagsakId)

    override fun equals(other: Any?) = other is ProsessId && internId == other.internId

    fun accept(visitor: ProsessIdVisitor) {
        eksterneIder.forEach { visitor.preVisit(this, internId, it) }
        eksterneIder.forEach { visitor.postVisit(this, internId, it) }
    }
}
