package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.modell.Person

interface PersonRepository {
    fun person(fnr: String): Person
}
