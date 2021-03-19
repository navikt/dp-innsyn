package no.nav.dagpenger.innsyn.db

import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Søknadsprosess

class PostgresPersonRepository : PersonRepository {
    override fun person(fnr: String): Person = getPerson(fnr) ?: lagPerson(fnr)

    override fun lagre(person: Person): Boolean =
        using(sessionOf(dataSource)) { session ->
            session.transaction { tx ->
                val personId = tx.run(
                    queryOf(
                        //language=PostgreSQL
                        "INSERT INTO person (fnr) VALUES (:fnr) ON CONFLICT (fnr) DO UPDATE SET fnr = :fnr",
                        mapOf("fnr" to person.fnr)
                    ).asUpdate
                )
                tx.run(
                    queryOf(
                        //language=PostgreSQL
                        "INSERT INTO søknad (person_id, ekstern_id) VALUES (?, ?) ON CONFLICT (person_id, ekstern_id) DO NOTHING",
                        personId,
                        "123"
                    ).asUpdate
                )
            }
        } == 1

    private fun lagPerson(fnr: String): Person = Person(fnr).also { lagre(it) }

    private fun getPerson(fnr: String): Person? =
        using(sessionOf(dataSource)) { session ->
            session.run(selectPerson(fnr))?.let {
                val søknader = hentSøknader(session, it)
                Person(fnr, søknader.toMutableList())
            }
        }

    private fun hentSøknader(session: Session, personId: Int): List<Søknadsprosess> = session.run(
        queryOf(
            //language=PostgreSQL
            "SELECT ekstern_id FROM søknad WHERE person_id=?",
            personId
        ).map { row ->
            Søknadsprosess(row.string(1), listOf())
        }.asList
    )

    private fun selectPerson(fnr: String) = //language=PostgreSQL
        queryOf(
            "SELECT person_id FROM person WHERE fnr = ?", fnr
        ).map { it.int(1) }.asSingle
}
