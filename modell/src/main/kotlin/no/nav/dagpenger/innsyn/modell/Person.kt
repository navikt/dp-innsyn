package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.Søknad.Tilstand.Innsendt

internal class Person(fnr: String) {
    private val søknader: MutableList<Søknad> = mutableListOf()
    private val vedtak: MutableList<Vedtak> = mutableListOf()

    fun håndter(søknad: Søknad) {
        søknader.add(søknad)
    }

    fun håndter(vedtak: Vedtak) {
        this.vedtak.add(vedtak)
        søknader.forEach{it.håndter(vedtak)}
    }

    fun håndter(ettersending: Ettersending){
        søknader.forEach{it.håndter(ettersending)}
    }

    fun harSøknadUnderBehandling() = søknader.any { it.tilstand is Innsendt }
}
