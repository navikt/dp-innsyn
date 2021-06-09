package no.nav.dagpenger.innsyn.db

import kotliquery.Query
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.serde.PersonData
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor

class PostgresPersonRepository() : PersonRepository {
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

    private fun getPerson(fnr: String) = using(sessionOf(dataSource)) { session ->
        session.run(selectPerson(fnr))?.let {
            val søknader = hentSøknaderFor(session, it)
            val vedtak = hentVedtakFor(session, it)
            PersonData(fnr, søknader, vedtak).person
        }
    }

    private fun hentSøknaderFor(session: Session, personId: Int) = session.run(
        queryOf( //language=PostgreSQL
            "SELECT * FROM søknad WHERE person_id = ?", personId
        ).map { row ->
            Søknad(
                søknadId = row.string("søknad_id"),
                journalpostId = row.string("journalpost_id"),
                skjemaKode = row.string("skjema_kode"),
                søknadsType = Søknad.SøknadsType.valueOf(row.string("søknads_type")),
                kanal = Kanal.valueOf(row.string("kanal"))
            )
        }.asList
    )

    private fun hentVedtakFor(session: Session, personId: Int) = session.run(
        queryOf( //language=PostgreSQL
            "SELECT * FROM vedtak WHERE person_id = ?", personId
        ).map { row ->
            Vedtak(
                vedtakId = row.string("vedtak_id"),
                fagsakId = row.string("fagsak_id"),
                status = Vedtak.Status.valueOf(row.string("status"))
            )
        }.asList
    )

    private fun selectPerson(fnr: String) = queryOf( //language=PostgreSQL
        "SELECT person_id FROM person WHERE fnr = ?", fnr
    ).map { it.int(1) }.asSingle

    private class PersonLagrer(person: Person) : PersonVisitor {
        val queries = mutableListOf<Query>()
        private lateinit var fnr: String

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

        override fun visitSøknad(
            søknadId: String?,
            journalpostId: String,
            skjemaKode: String?,
            søknadsType: Søknad.SøknadsType,
            kanal: Kanal
        ) {
            queries.add(
                queryOf(
                    //language=PostgreSQL
                    """INSERT INTO søknad(person_id, søknad_id, journalpost_id, skjema_kode, søknads_type, kanal)
                        VALUES ((SELECT person_id FROM person WHERE fnr = :fnr), :soknadId, :journalpostId, :skjemaKode, :soknadsType, :kanal)
                        ON CONFLICT DO NOTHING
                    """.trimMargin(),
                    mapOf(
                        "fnr" to fnr,
                        "soknadId" to søknadId,
                        "journalpostId" to journalpostId,
                        "skjemaKode" to skjemaKode,
                        "soknadsType" to søknadsType.toString(),
                        "kanal" to kanal.toString(),
                    )
                )
            )
        }

        override fun visitVedtak(vedtakId: String, fagsakId: String, status: Vedtak.Status) {
            queries.add(
                queryOf(
                    //language=PostgreSQL
                    """INSERT INTO vedtak(person_id, vedtak_id, fagsak_id, status)
                        VALUES ((SELECT person_id FROM person WHERE fnr = :fnr), :vedtakId, :fagsakId, :status)
                        ON CONFLICT DO NOTHING
                    """.trimMargin(),
                    mapOf(
                        "fnr" to fnr,
                        "vedtakId" to vedtakId,
                        "fagsakId" to fagsakId,
                        "status" to status.toString(),
                    )
                )
            )
        }
    }
}
