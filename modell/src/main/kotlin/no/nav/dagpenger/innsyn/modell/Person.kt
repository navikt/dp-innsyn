package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.Søknad.Companion.erInnsendt

internal class Person private constructor(
    private val fnr: String,
    private val tidslinje: Tidslinje
) {
    constructor(fnr: String) : this(fnr, Tidslinje())

    private val søknader get() = tidslinje.filterIsInstance<Søknad>()
    private val vedtak get() = tidslinje.filterIsInstance<Vedtak>()

    fun håndter(søknad: Søknad) {
        tidslinje.leggTil(søknad)
    }

    fun håndter(vedtak: Vedtak) {
        tidslinje.leggTil(vedtak)
        søknader.forEach { it.håndter(vedtak) }
    }

    fun håndter(ettersending: Ettersending) {
        tidslinje.leggTil(ettersending)
        søknader.forEach { it.håndter(ettersending) }
    }

    fun harSøknadUnderBehandling() = søknader.any(::erInnsendt)
}
