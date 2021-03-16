package no.nav.dagpenger.innsyn.modell

internal class Tidslinje private constructor(
    private val hendelser: MutableList<Hendelse>
) : Iterable<Hendelse> by hendelser {
    constructor() : this(mutableListOf())

    internal fun leggTil(hendelse: Hendelse) {
        hendelser.add(hendelse)
    }
}
