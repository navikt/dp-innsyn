package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.Søknad.Tilstand.Innsendt

internal class Person(fnr: String) {

    val tidslinje: Tidslinje

    init {
        tidslinje = Tidslinje()
    }

    private val søknader: MutableList<Søknad> = mutableListOf()
    private val vedtak: MutableList<Vedtak> = mutableListOf()

    fun håndter(søknadHendelse: SøknadHendelse) {
        tidslinje.leggTilHendelse(søknadHendelse)
        søknader.add(søknadHendelse.søknad())
    }

    fun håndter(vedtakHendelse: VedtakHendelse) {
        tidslinje.leggTilHendelse(vedtakHendelse)
        vedtak.add(vedtakHendelse.vedtak())
    }

    fun håndter(ettersendingHendelse: EttersendingHendelse) {
        tidslinje.leggTilHendelse(ettersendingHendelse)
    }

    fun harSøknadUnderBehandling() = søknader.any { it.tilstand is Innsendt }
}
