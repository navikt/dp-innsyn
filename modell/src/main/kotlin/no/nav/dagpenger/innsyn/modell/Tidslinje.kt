package no.nav.dagpenger.innsyn.modell

internal class Tidslinje {

    var hendelser = mutableListOf<Hendelse>()

    fun leggTilHendelse(hendelse: Hendelse) {
        hendelser.add(hendelse)
    }
}
