package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad

class PersonData(private val fnr: String, private val søknader: List<Søknad>) {

    val person: Person
        get() =
            Person(fnr).also {
                it.javaClass.getDeclaredField("søknader").apply {
                    isAccessible = true
                }.set(it, søknader.toMutableList())
            }
}