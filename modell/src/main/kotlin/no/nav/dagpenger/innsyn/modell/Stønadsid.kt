package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Journalføring
import no.nav.dagpenger.innsyn.modell.hendelser.Mangelbrev
import no.nav.dagpenger.innsyn.modell.hendelser.Saksbehandling
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.serde.StønadsidVisitor
import java.util.UUID

class Stønadsid(
    private val internId: UUID,
    private val eksterneIder: MutableList<String>
) {
    constructor() : this(UUID.randomUUID(), mutableListOf())

    fun håndter(søknad: Søknad): Boolean {
        eksterneIder.add(søknad.søknadId)
        eksterneIder.add(søknad.journalpostId)
        return true
    }

    fun håndter(journalføring: Journalføring): Boolean {
        if (!eksterneIder.contains(journalføring.journalpostId)) return false
        eksterneIder.add(journalføring.fagsakId)
        return true
    }

    fun håndter(ettersending: Ettersending): Boolean {
        if (eksterneIder.contains(ettersending.søknadId)) return true
        return false
    }

    fun håndter(vedtak: Vedtak): Boolean {
        if (eksterneIder.contains(vedtak.fagsakId)) return true
        return false
    }

    fun håndter(mangelbrev: Mangelbrev): Boolean {
        return true
    }

    fun håndter(saksbehandling: Saksbehandling): Boolean {
        return true
    }

    override fun equals(other: Any?) = other is Stønadsid && internId == other.internId

    fun accept(visitor: StønadsidVisitor) {
        eksterneIder.forEach { visitor.preVisit(this, internId, it) }
        eksterneIder.forEach { visitor.postVisit(this, internId, it) }
    }
}
