package no.nav.dagpenger.innsyn.modell

class Person(
    val fnr: String,
    søknader: List<Søknad>
) {
    private val søknader: MutableList<Søknad> = søknader.toMutableList()

    constructor(fnr: String) : this(fnr, listOf())

    fun harSøknadUnderBehandling() = søknader.any { !it.harVedtak() }

    fun håndter(søknad: Søknad) {
        søknader.add(søknad)
    }

    fun håndter(vedtak: Vedtak) {
        søknader.forEach { it.håndter(vedtak) }
    }
}
