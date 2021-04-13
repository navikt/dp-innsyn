package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Hendelse

internal class Tidslinje(private val hendelser: MutableList<Hendelse>) : Collection<Hendelse> by hendelser {

    constructor() : this(mutableListOf())

    fun leggTil(hendelse: Hendelse) = hendelser.add(hendelse)
}
