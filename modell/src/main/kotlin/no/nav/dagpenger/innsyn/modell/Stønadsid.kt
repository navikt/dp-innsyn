package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Mangelbrev
import no.nav.dagpenger.innsyn.modell.hendelser.Saksbehandling
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.serde.StønadsforholdVisitor
import no.nav.dagpenger.innsyn.modell.serde.StønadsidVisitor

class Stønadsid(
    private val internId: String,
    private val eksterneIder: MutableList<String>
) {
    constructor() : this("", mutableListOf())

    fun håndter(søknad: Søknad): Boolean {
        eksterneIder.add(søknad.søknadId)
        eksterneIder.add(søknad.fagsakId)
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