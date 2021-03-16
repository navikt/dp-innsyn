package no.nav.dagpenger.innsyn.db

import no.nav.dagpenger.innsyn.PersonRepository
import no.nav.dagpenger.innsyn.modell.Person

class PostgresPersonRepository : PersonRepository {
    override fun person(fnr: String): Person {
        return Person(fnr)
    }
}
