package no.nav.dagpenger.innsyn.db

import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.PersonVisitor
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand.Ferdig
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand.Uferdig
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType

class PostgresPersonRepository : PersonRepository {
    override fun person(fnr: String): Person = getPerson(fnr) ?: lagPerson(fnr)

    override fun lagre(person: Person): Boolean {
        val oppgaver = OppgaveVisitor(person).oppgaver

        return using(sessionOf(dataSource)) { session ->
            session.transaction { tx ->
                val personId = tx.run(
                    queryOf(
                        //language=PostgreSQL
                        "INSERT INTO person (fnr) VALUES (:fnr) ON CONFLICT (fnr) DO UPDATE SET fnr = :fnr",
                        mapOf("fnr" to person.fnr)
                    ).asUpdate
                )
                oppgaver.map {
                    tx.run(
                        queryOf(
                            //language=PostgreSQL
                            "INSERT INTO oppgave (person_id, id, type, tilstand) VALUES (:personId, :id, :type, :tilstand) ON CONFLICT (person_id, id, type) DO UPDATE SET tilstand = :tilstand",
                            mapOf("personId" to personId) + it
                        ).asUpdate
                    )
                }.all { it == 1 }
            }
        }
    }

    private fun lagPerson(fnr: String): Person = Person(fnr).also { lagre(it) }

    private fun getPerson(fnr: String): Person? =
        using(sessionOf(dataSource)) { session ->
            session.run(selectPerson(fnr))?.let {
                val oppgaver = hentOppgaver(session, it)
                Person(fnr, oppgaver)
            }
        }

    private fun hentOppgaver(session: Session, personId: Int): List<Oppgave> = session.run(
        queryOf(
            //language=PostgreSQL
            "SELECT id, type, tilstand FROM oppgave WHERE person_id=?",
            personId
        ).map { row ->
            val oppgaveType = row.string(2)
            when (row.string(3)) {
                Uferdig.toString() -> OppgaveType(oppgaveType).ny(row.string(1), "")
                Ferdig.toString() -> OppgaveType(oppgaveType).ferdig(row.string(1), "")
                else -> throw IllegalArgumentException("ukjent tilstand")
            }
        }.asList
    )

    private fun selectPerson(fnr: String) = //language=PostgreSQL
        queryOf(
            "SELECT person_id FROM person WHERE fnr = ?", fnr
        ).map { it.int(1) }.asSingle

    private class OppgaveVisitor(person: Person) : PersonVisitor {
        val oppgaver = mutableListOf<Map<String, String>>()

        init {
            person.accept(this)
        }

        override fun preVisit(
            oppgave: Oppgave,
            id: String,
            oppgaveType: OppgaveType,
            tilstand: OppgaveTilstand
        ) {
            oppgaver.add(mapOf("id" to id, "type" to oppgaveType.toString(), "tilstand" to tilstand.toString()))
        }
    }
}
