package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.Søknad.Tilstand.Innsendt

class Person(fnr: String) {
    private val søknader: MutableList<Søknad> = mutableListOf()
    private val vedtak: MutableList<Vedtak> = mutableListOf()

    fun håndter(hendelse: SøknadHendelse) {
        søknader.add(Søknad(hendelse.id))
    }

    fun håndter(hendelse: VedtakHendelse) {
        vedtak.add(Vedtak(hendelse.vedtakId))
    }

    fun harSøknadUnderBehandling() = søknader.any { it.tilstand is Innsendt }
}
