package no.nav.dagpenger.innsyn.modell

internal class Tidslinje private constructor(
    private val hendelser: MutableList<Hendelse>,
    private val observatører: MutableSet<TidslinjeObservatør>
) : Iterable<Hendelse> by hendelser {
    constructor() : this(mutableListOf(), mutableSetOf())

    fun leggTil(hendelse: Hendelse) {
        hendelser.add(hendelse)
    }

    fun lytt(observatør: TidslinjeObservatør) = observatører.add(observatør)

    interface TidslinjeObservatør {
        fun hendelse(hendelse: Hendelse)
    }
}
