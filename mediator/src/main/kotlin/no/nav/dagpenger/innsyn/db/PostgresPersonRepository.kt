package no.nav.dagpenger.innsyn.db

import kotliquery.Query
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.ProsessId
import no.nav.dagpenger.innsyn.modell.Søknadsprosess
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveId
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import no.nav.dagpenger.innsyn.modell.serde.OppgaveData
import no.nav.dagpenger.innsyn.modell.serde.PersonData
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor
import no.nav.dagpenger.innsyn.modell.serde.SøknadsprosessData
import java.time.LocalDateTime
import java.util.UUID

class PostgresPersonRepository : PersonRepository {
    override fun person(fnr: String): Person = getPerson(fnr) ?: lagPerson(fnr)

    override fun lagre(person: Person): Boolean {
        val visitor = PersonLagrer(person)

        return using(sessionOf(dataSource)) { session ->
            session.transaction { tx ->
                visitor.queries.map {
                    tx.run(it.asUpdate)
                }.all { it >= 1 }
            }
        }
    }

    private fun lagPerson(fnr: String): Person = Person(fnr).also { lagre(it) }

    private fun getPerson(fnr: String): Person? =
        using(sessionOf(dataSource)) { session ->
            session.run(selectPerson(fnr))?.let {
                val stønadsforhold = hentSøknadsprosess(session, it)

                PersonData(fnr, stønadsforhold).person
            }
        }

    private fun hentSøknadsprosess(session: Session, personId: Int) = session.run(
        queryOf(
            //language=PostgreSQL
            "SELECT søknadsprosess_id, tilstand FROM søknadsprosesser WHERE person_id=?",
            personId
        ).map { row ->
            val stønadsforholdId = UUID.fromString(row.string("søknadsprosess_id"))
            SøknadsprosessData(
                hentProsessId(session, stønadsforholdId),
                hentOppgaver(session, stønadsforholdId),
                row.string("tilstand")
            )
        }.asList
    )

    private fun hentProsessId(session: Session, stønadsforholdId: UUID) = session.run(
        queryOf(
            //language=PostgreSQL
            "SELECT ekstern_id FROM prosess_id WHERE søknadsprosess_id = ?",
            stønadsforholdId
        ).map { row ->
            row.string("ekstern_id")
        }.asList
    ).run {
        ProsessId(stønadsforholdId, this.toMutableList())
    }

    private fun hentOppgaver(session: Session, stønadsforholdId: UUID): List<OppgaveData> = session.run(
        queryOf(
            //language=PostgreSQL
            """SELECT oppgave_id, id, beskrivelse, opprettet, type, tilstand
                FROM oppgave
                WHERE søknadsprosess_id = ?
            """.trimIndent(),
            stønadsforholdId
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

    private class PersonLagrer(person: Person) : PersonVisitor {
        val queries = mutableListOf<Query>()
        private lateinit var fnr: String
        private lateinit var aktivtStønadsforhold: UUID
        private lateinit var tilstand: String

        init {
            person.accept(this)
        }

        override fun preVisit(person: Person, fnr: String) {
            this.fnr = fnr
            queries.add(
                queryOf( //language=PostgreSQL
                    "INSERT INTO person (fnr) VALUES (:fnr) ON CONFLICT DO NOTHING",
                    mapOf("fnr" to fnr)
                )
            )
        }

        override fun preVisit(søknadsprosess: Søknadsprosess, tilstand: Søknadsprosess.Tilstand) {
            this.tilstand = tilstand.type.toString()
        }

        override fun preVisit(stønadsid: ProsessId, internId: UUID, eksternId: String) {
            aktivtStønadsforhold = internId
            queries.add(
                queryOf(
                    //language=PostgreSQL
                    """INSERT INTO søknadsprosesser(søknadsprosess_id, person_id, tilstand)
                        VALUES (:stonadsforhold, (SELECT person_id FROM person WHERE fnr = :fnr), :tilstand)
                        ON CONFLICT (søknadsprosess_id) DO UPDATE SET tilstand = :tilstand
                    """.trimIndent(),
                    mapOf(
                        "stonadsforhold" to aktivtStønadsforhold,
                        "fnr" to fnr,
                        "tilstand" to tilstand
                    )
                )
            )
            queries.add(
                queryOf(
                    //language=PostgreSQL
                    """INSERT INTO prosess_id(søknadsprosess_id, ekstern_id)
                        VALUES (:stonadsforholdId, :eksternId)
                        ON CONFLICT (søknadsprosess_id, ekstern_id) DO NOTHING
                    """.trimIndent(),
                    mapOf(
                        "stonadsforholdId" to internId,
                        "eksternId" to eksternId,
                    )
                )
            )
        }

        override fun preVisit(
            oppgave: Oppgave,
            id: OppgaveId,
            beskrivelse: String,
            opprettet: LocalDateTime,
            tilstand: OppgaveTilstand
        ) {
            queries.add(
                queryOf( //language=PostgreSQL
                    """INSERT INTO oppgave (id, søknadsprosess_id, beskrivelse, opprettet, type, tilstand)
                        VALUES (:id, :stonadsforhold, :beskrivelse, :opprettet, :type, :tilstand)
                        ON CONFLICT (id, type, søknadsprosess_id) DO UPDATE SET tilstand = :tilstand
                    """.trimIndent(),
                    mapOf(
                        "id" to id.id,
                        "stonadsforhold" to aktivtStønadsforhold,
                        "beskrivelse" to beskrivelse,
                        "opprettet" to opprettet,
                        "type" to id.type.toString(),
                        "tilstand" to tilstand.toString()
                    )
                )
            )
        }
    }
}
