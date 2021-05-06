package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Person

class PersonData(
    fnr: String,
    søknadsprosess: List<SøknadsprosessData>,
) {
    private val søknadsprosesser by lazy { søknadsprosess.map { it.søknadsprosess }.toMutableSet() }
    val person: Person by lazy {
        Person(fnr).also {
            it.javaClass.getDeclaredField("søknadsprosesser").apply {
                isAccessible = true
            }.set(it, this.søknadsprosesser)
        }
    }
}
