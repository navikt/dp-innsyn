package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Person

class PersonData(
    fnr: String,
    stønadsforhold: List<StønadsforholdData>,
) {
    private val stønadsforhold by lazy { stønadsforhold.map { it.stønadsforhold }.toMutableSet() }
    val person: Person by lazy {
        Person(fnr).also {
            it.javaClass.getDeclaredField("stønadsforhold").apply {
                isAccessible = true
            }.set(it, this.stønadsforhold)
        }
    }
}
