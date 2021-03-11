package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.Søknad.Tilstand.Innsendt

internal class Person private constructor(
    private val fnr: String,
    private val tidslinje: Tidslinje,
    private val søknader: MutableList<Søknad>,
    private val vedtak: MutableList<Vedtak>
) {
    constructor(fnr: String) : this(fnr, Tidslinje(), mutableListOf(), mutableListOf())

    fun håndter(søknadHendelse: SøknadHendelse) {
        tidslinje.leggTil(søknadHendelse)
        søknader.add(søknadHendelse.søknad())
    }

    fun håndter(vedtakHendelse: VedtakHendelse) {
        tidslinje.leggTil(vedtakHendelse)
        vedtak.add(vedtakHendelse.vedtak())
    }

    fun håndter(ettersendingHendelse: EttersendingHendelse) {
        tidslinje.leggTil(ettersendingHendelse)
    }

    fun harSøknadUnderBehandling() = søknader.any { it.tilstand is Innsendt }
}
