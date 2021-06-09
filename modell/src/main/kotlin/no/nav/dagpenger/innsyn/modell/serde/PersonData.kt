package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak

class PersonData(private val fnr: String, private val søknader: List<Søknad>, private val vedtak: List<Vedtak>) {

    val person: Person
        get() =
            Person(fnr).also {
                it.setPrivatListe("vedtak", vedtak)
                it.setPrivatListe("søknader", søknader)
            }

    private fun Person.setPrivatListe(navn: String, list: List<Any>) {
        this.javaClass.getDeclaredField(navn).apply {
            isAccessible = true
        }.set(this, list.toMutableList())
    }
}
