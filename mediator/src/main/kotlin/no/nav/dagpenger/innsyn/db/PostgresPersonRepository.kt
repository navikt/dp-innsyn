package no.nav.dagpenger.innsyn.db

import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.innsyn.modell.Behandlingskjede
import no.nav.dagpenger.innsyn.modell.BehandlingskjedeId
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveId
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import no.nav.dagpenger.innsyn.modell.serde.OppgaveData
import no.nav.dagpenger.innsyn.modell.serde.PersonData
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor
import java.time.LocalDateTime

class PostgresPersonRepository : PersonRepository {
    override fun person(fnr: String): Person = getPerson(fnr) ?: lagPerson(fnr)

    override fun lagre(person: Person): Boolean {
        val visitor = OppgaveVisitor(person)
        val kjeder = visitor.kjeder
        val oppgaver = visitor.oppgaver

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
                            """INSERT INTO oppgave (person_id, id, beskrivelse, opprettet, type, tilstand)
                                VALUES (:personId, :id, :beskrivelse, :opprettet, :type, :tilstand)
                                ON CONFLICT (person_id, id, type) DO UPDATE SET tilstand = :tilstand
                            """.trimIndent(),
                            mapOf("personId" to personId) + it
                        ).asUpdate
                    )
                }.all { it == 1 }
                kjeder.map { (kjedeId, oppgaveId) ->
                    tx.run(
                        queryOf(
                            //language=PostgreSQL
                            """INSERT INTO behandlingskjede(id, oppgave_id, person_id)
                            VALUES (:id, (SELECT oppgave_id
                                          FROM oppgave
                                          WHERE id = :oppgaveId AND type = :oppgaveType AND person_id = :personId), :personId)
                            ON CONFLICT (id, oppgave_id, person_id) DO NOTHING 
                            """.trimIndent(),
                            mapOf(
                                "personId" to personId,
                                "id" to kjedeId,
                                "oppgaveId" to oppgaveId.first,
                                "oppgaveType" to oppgaveId.second
                            )
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
                val kjeder = hentKjeder(session, it)
                PersonData(fnr, kjeder, oppgaver).person
            }
        }

    private fun hentKjeder(session: Session, personId: Int) = session.run(
        queryOf(
            //language=PostgreSQL
            "SELECT id, oppgave_id FROM behandlingskjede WHERE person_id=?",
            personId
        ).map { row ->
            row.string("id") to row.string("oppgave_id")
        }.asList
    ).groupBy({ it.first }, { it.second })

    private fun hentOppgaver(session: Session, personId: Int): Map<String, Oppgave> = session.run(
        queryOf(
            //language=PostgreSQL
            "SELECT oppgave_id, id, beskrivelse, opprettet, type, tilstand FROM oppgave WHERE person_id=?",
            personId
        ).map { row ->
            row.string("oppgave_id") to
                OppgaveData(
                    row.int("oppgave_id"),
                    row.string("id"),
                    row.string("beskrivelse"),
                    row.localDateTime("opprettet"),
                    row.string("type"),
                    row.string("tilstand")
                ).oppgave
        }.asList
    ).associateBy({ it.first }, { it.second })

    private fun selectPerson(fnr: String) = //language=PostgreSQL
        queryOf(
            "SELECT person_id FROM person WHERE fnr = ?", fnr
        ).map { it.int(1) }.asSingle

    private class OppgaveVisitor(person: Person) : PersonVisitor {
        val oppgaver = mutableListOf<Map<String, Any>>()
        lateinit var aktivKjede: String
        val kjeder = mutableListOf<Pair<String, Pair<String, String>>>()

        init {
            person.accept(this)
        }

        override fun preVisit(behandlingskjede: Behandlingskjede, id: BehandlingskjedeId) {
            aktivKjede = id
        }

        override fun preVisit(
            oppgave: Oppgave,
            id: OppgaveId,
            beskrivelse: String,
            opprettet: LocalDateTime,
            tilstand: OppgaveTilstand
        ) {
            kjeder.add(aktivKjede to (id.id to id.type.toString()))

            oppgaver.add(
                mapOf(
                    "id" to id.id,
                    "beskrivelse" to beskrivelse,
                    "opprettet" to opprettet,
                    "type" to id.type.toString(),
                    "tilstand" to tilstand.toString()
                )
            )
        }
    }
}
