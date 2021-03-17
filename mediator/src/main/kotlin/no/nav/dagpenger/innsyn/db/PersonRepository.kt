package no.nav.dagpenger.innsyn.db

import no.nav.dagpenger.innsyn.modell.Person

interface PersonRepository {
    fun person(fnr: String): Person
    fun lagre(person: Person): Boolean
}
