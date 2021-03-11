package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.Søknad.Companion.erInnsendt

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
        vedtakHendelse.vedtak().also { vedtak ->
            this.vedtak.add(vedtak)
            søknader.forEach { it.håndter(vedtak) }
        }
    }

    fun håndter(ettersendingHendelse: EttersendingHendelse) {
        tidslinje.leggTil(ettersendingHendelse)
        ettersendingHendelse.ettersending().also { ettersending ->
            søknader.forEach { it.håndter(ettersending) }
        }
    }

    fun harSøknadUnderBehandling() = søknader.any(::erInnsendt)
}
