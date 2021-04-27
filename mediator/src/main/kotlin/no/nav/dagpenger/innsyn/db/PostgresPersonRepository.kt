package no.nav.dagpenger.innsyn.db

import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.Stønadsforhold
import no.nav.dagpenger.innsyn.modell.Stønadsid
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveId
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import no.nav.dagpenger.innsyn.modell.serde.OppgaveData
import no.nav.dagpenger.innsyn.modell.serde.PersonData
import no.nav.dagpenger.innsyn.modell.serde.StønadsforholdData
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
                stønadsforhold.map { data ->
                    tx.run(
                        queryOf(
                            //language=PostgreSQL
                            """INSERT INTO stønadsforhold(intern_id, person_id, tilstand)
                            VALUES (:internId, :personId, :tilstand)
                            ON CONFLICT (intern_id, person_id) DO UPDATE SET tilstand=:tilstand
                            """.trimIndent(),
                            data + mapOf(
                                "personId" to personId,
                            )
                        ).asUpdate
                    )
                }
                oppgaver.map {
                    tx.run(
                        queryOf(
                            //language=PostgreSQL
                            """INSERT INTO oppgave (id, stønadsforhold_id, beskrivelse, opprettet, type, tilstand)
                                VALUES (:id, (SELECT id FROM stønadsforhold WHERE intern_id = :stonadsforholdId), :beskrivelse, :opprettet, :type, :tilstand)
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
                val stønadsforhold = hentStønadsforhold(session, it)

                PersonData(fnr, stønadsforhold).person
            }
        }

    private fun hentStønadsforhold(session: Session, personId: Int) = session.run(
        queryOf(
            //language=PostgreSQL
            "SELECT id, intern_id, tilstand FROM stønadsforhold WHERE person_id=?",
            personId
        ).map { row ->
            StønadsforholdData(row.string("intern_id"), hentOppgaver(session, row.int("id")), row.string("tilstand"))
        }.asList
    )

    private fun hentOppgaver(session: Session, internId: Int): List<OppgaveData> = session.run(
        queryOf(
            //language=PostgreSQL
            "SELECT oppgave_id, id, beskrivelse, opprettet, type, tilstand FROM oppgave WHERE stønadsforhold_id =?",
            internId
        ).map { row ->
                OppgaveData(
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
        val stønadsforhold = mutableListOf<Map<String, Any>>()

        init {
            person.accept(this)
        }

        override fun preVisit(stønadsid: Stønadsid, internId: String, eksternId: String) {
            aktivtStønadsforhold = internId
        }

        override fun postVisit(stønadsforhold: Stønadsforhold, tilstand: Stønadsforhold.Tilstand) {
            this.stønadsforhold.add(mapOf("internId" to aktivtStønadsforhold, "tilstand" to tilstand.toString()))
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
                    "stonadsforholdId" to aktivtStønadsforhold,
                    "beskrivelse" to beskrivelse,
                    "opprettet" to opprettet,
                    "type" to id.type.toString(),
                    "tilstand" to tilstand.toString()
                )
            )
        }
    }
}
