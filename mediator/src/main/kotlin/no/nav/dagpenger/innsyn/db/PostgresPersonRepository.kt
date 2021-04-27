package no.nav.dagpenger.innsyn.db

import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.Stønadsid
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveId
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import no.nav.dagpenger.innsyn.modell.serde.OppgaveData
import no.nav.dagpenger.innsyn.modell.serde.PersonData
import java.time.LocalDateTime

class PostgresPersonRepository : PersonRepository {
    override fun person(fnr: String): Person = getPerson(fnr) ?: lagPerson(fnr)

    override fun lagre(person: Person): Boolean {
        val visitor = PersonVisitor(person)
        val stønadsforhold = visitor.stønadsforhold
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
                stønadsforhold.map { internId ->
                    tx.run(
                        queryOf(
                            //language=PostgreSQL
                            """INSERT INTO stønadsforhold(intern_id, person_id)
                            VALUES (:internId, :personId)
                            ON CONFLICT (intern_id, person_id) DO NOTHING 
                            """.trimIndent(),
                            mapOf(
                                "internId" to internId,
                                "personId" to personId
                            )
                        ).asUpdate
                    )
                }
                oppgaver.map {
                    tx.run(
                        queryOf(
                            //language=PostgreSQL
                            """INSERT INTO oppgave (id, stønadsforhold_id, beskrivelse, opprettet, type, tilstand)
                                VALUES (:id, (SELECT id FROM stønadsforhold WHERE intern_id = :stønadsforholdId), :beskrivelse, :opprettet, :type, :tilstand)
                                ON CONFLICT (id, type) DO UPDATE SET tilstand = :tilstand
                            """.trimIndent(),
                            it
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
                val stønadsforhold = hentStønadsforhold(session, it)
                PersonData(fnr, stønadsforhold, oppgaver).person
            }
        }

    private fun hentStønadsforhold(session: Session, personId: Int) = session.run(
        queryOf(
            //language=PostgreSQL
            "SELECT intern_id FROM stønadsforhold WHERE person_id=?",
            personId
        ).map { row ->
            row.string("intern_id")
        }.asList
    )

    private fun hentOppgaver(session: Session, personId: Int): List<OppgaveData> = session.run(
        queryOf(
            //language=PostgreSQL
            "SELECT oppgave_id, stønadsforhold_id, id, beskrivelse, opprettet, type, tilstand FROM oppgave WHERE stønadsforhold_id IN (SELECT intern_id FROM stønadsforhold WHERE person_id =?)",
            personId
        ).map { row ->
                OppgaveData(
                    row.string("stønadsforhold"),
                    row.int("oppgave_id"),
                    row.string("id"),
                    row.string("beskrivelse"),
                    row.localDateTime("opprettet"),
                    row.string("type"),
                    row.string("tilstand")
                )
        }.asList
    )

    private fun selectPerson(fnr: String) = //language=PostgreSQL
        queryOf(
            "SELECT person_id FROM person WHERE fnr = ?", fnr
        ).map { it.int(1) }.asSingle

    private class PersonVisitor(person: Person) :
        no.nav.dagpenger.innsyn.modell.serde.PersonVisitor {
        val oppgaver = mutableListOf<Map<String, Any>>()
        lateinit var aktivtStønadsforhold: String
        val stønadsforhold = mutableSetOf<String>()

        init {
            person.accept(this)
        }

        override fun preVisit(stønadsid: Stønadsid, internId: String, eksternId: String) {
            aktivtStønadsforhold = internId
            stønadsforhold.add(internId)
        }

        override fun preVisit(
            oppgave: Oppgave,
            id: OppgaveId,
            beskrivelse: String,
            opprettet: LocalDateTime,
            tilstand: OppgaveTilstand
        ) {
            oppgaver.add(
                mapOf(
                    "id" to id.id,
                    "stønadsforholdId" to aktivtStønadsforhold,
                    "beskrivelse" to beskrivelse,
                    "opprettet" to opprettet,
                    "type" to id.type.toString(),
                    "tilstand" to tilstand.toString()
                )
            )
        }
    }
}
