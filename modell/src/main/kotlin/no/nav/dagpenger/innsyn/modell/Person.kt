package no.nav.dagpenger.innsyn.modell

class Person private constructor(
    val fnr: String,
    private val søknader: MutableList<Søknad>
) {
    constructor(fnr: String) : this(fnr, mutableListOf())

    fun harSøknadUnderBehandling() = søknader.any { !it.harVedtak() }

    fun håndter(søknad: Søknad) {
        søknader.add(søknad)
    }

    fun håndter(vedtak: Vedtak) {
        søknader.forEach { it.håndter(vedtak) }
    }
}
