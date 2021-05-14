package no.nav.dagpenger.innsyn.helpers

import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.modell.Person

internal class InMemoryPersonRepository : PersonRepository {
    private val personer = mutableMapOf<String, Person>()

    override fun person(fnr: String) = personer.getOrPut(fnr) { Person(fnr) }
    override fun lagre(person: Person) = personer.put(person.fnr, person) !== null
}
