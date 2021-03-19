package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Søknadsprosess

class Person(
    val fnr: String,
    søknader: List<Søknadsprosess>
) {
    constructor(fnr: String) : this(fnr, listOf())

    private val søknader: MutableList<Søknadsprosess> = søknader.toMutableList()

    fun harSøknadUnderBehandling() = søknader.any { it.harUferdigeOppgaver() }

    fun håndter(søknadsprosess: Søknadsprosess) {
        søknader.add(søknadsprosess)
    }
}
