package no.nav.dagpenger.innsyn.modell

class Tidslinje {

    var hendelser = mutableListOf<Any>()

    fun leggTilSøknadsHendelse(hendelse: SøknadHendelse) {
        hendelser.add(hendelse)
    }

    fun leggTilVedtakHendelse(hendelse: VedtakHendelse) {
        hendelser.add(hendelse)
    }
}
