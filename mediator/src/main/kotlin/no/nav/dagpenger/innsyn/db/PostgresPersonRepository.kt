package no.nav.dagpenger.innsyn.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.innsyn.PersonRepository
import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.innsyn.modell.Person

class PostgresPersonRepository : PersonRepository {
    override fun person(fnr: String): Person = getPerson(fnr) ?: lagPerson(fnr)

    override fun lagre(person: Person): Boolean =
        using(sessionOf(dataSource)) { session ->
            session.transaction { tx ->
                tx.run(
                    queryOf(
                        //language=PostgreSQL
                        "INSERT INTO person (fnr) VALUES (:fnr) ON CONFLICT (fnr) DO UPDATE SET fnr = :fnr",
                        mapOf("fnr" to person.fnr)
                    ).asUpdate
                )
                tx.run(
                    queryOf(
                        //language=PostgreSQL
                        "INSERT INTO søknad (person_id, ekstern_id) VALUES (:fnr) ON CONFLICT (fnr) DO UPDATE SET fnr = :fnr",
                        mapOf("fnr" to person.fnr)
                    ).asUpdate
                )
            }
        } == 1

    private fun lagPerson(fnr: String): Person = Person(fnr).also { lagre(it) }

    private fun getPerson(fnr: String): Person? =
        using(sessionOf(dataSource)) { session -> session.run(selectPerson(fnr)) }

    private fun selectPerson(fnr: String) = //language=PostgreSQL
        queryOf("SELECT * FROM person WHERE fnr = ?", fnr).map { Person(fnr) }.asSingle
}
