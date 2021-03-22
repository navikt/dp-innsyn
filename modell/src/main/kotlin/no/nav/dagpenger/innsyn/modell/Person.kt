package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad

class Person(
    val fnr: String,
    blæh: List<Søknadsprosess>
) {
    constructor(fnr: String) : this(fnr, listOf())

    private val blæh: MutableList<Søknadsprosess> = blæh.toMutableList()

    fun harSøknadUnderBehandling() = blæh.any { it.harUferdigeOppgaver() }
    fun harKomplettSøknad() = blæh.any { it.erKomplett() }

    fun håndter(søknad: Søknad) {
        val søknadsprosess = Søknadsprosess(søknad.søknadId, søknad.oppgaver)
        blæh.add(søknadsprosess)
    }

    fun håndter(ettersending: Ettersending) {
        blæh.forEach { it.håndter(ettersending) }
    }
}
