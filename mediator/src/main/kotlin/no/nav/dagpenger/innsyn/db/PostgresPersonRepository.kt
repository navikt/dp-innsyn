package no.nav.dagpenger.innsyn.db

import kotliquery.Query
import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad.SøknadsType
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.serde.PersonData
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor
import java.time.LocalDate
import java.time.LocalDateTime

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

    private fun getPerson(fnr: String) = using(sessionOf(dataSource)) { session ->
        session.run(selectPerson(fnr))?.let {
            val søknader = hentSøknaderFor(fnr)
            val vedtak = hentVedtakFor(session, it)
            PersonData(fnr, søknader, vedtak).person
        }
    }

    private fun hentVedtakFor(session: Session, personId: Int) = session.run(
        queryOf( //language=PostgreSQL
            "SELECT * FROM vedtak WHERE person_id = ?", personId
        ).map { row ->
            Vedtak(
                vedtakId = row.string("vedtak_id"),
                fagsakId = row.string("fagsak_id"),
                status = Vedtak.Status.valueOf(row.string("status")),
                datoFattet = row.localDateTime("fattet"),
                fraDato = row.localDateTime("fra_dato"),
                tilDato = row.localDateTimeOrNull("til_dato")
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
            søknadsType: SøknadsType,
            kanal: Kanal,
            datoInnsendt: LocalDateTime
        ) {
            queries.add(
                queryOf(
                    //language=PostgreSQL
                    """INSERT INTO søknad(person_id, søknad_id, journalpost_id, skjema_kode, søknads_type, kanal, dato_innsendt)
                        VALUES ((SELECT person_id FROM person WHERE fnr = :fnr), :soknadId, :journalpostId, :skjemaKode, :soknadsType, :kanal, :datoInnsendt)
                        ON CONFLICT DO NOTHING
                    """.trimMargin(),
                    mapOf(
                        "fnr" to fnr,
                        "soknadId" to søknadId,
                        "journalpostId" to journalpostId,
                        "skjemaKode" to skjemaKode,
                        "soknadsType" to søknadsType.toString(),
                        "kanal" to kanal.toString(),
                        "datoInnsendt" to datoInnsendt
                    )
                )
            )
        }

        override fun visitVedtak(
            vedtakId: String,
            fagsakId: String,
            status: Vedtak.Status,
            datoFattet: LocalDateTime,
            fraDato: LocalDateTime,
            tilDato: LocalDateTime?
        ) {
            queries.add(
                queryOf(
                    //language=PostgreSQL
                    """INSERT INTO vedtak(person_id, vedtak_id, fagsak_id, status, fattet, fra_dato, til_dato)
                        VALUES ((SELECT person_id FROM person WHERE fnr = :fnr), :vedtakId, :fagsakId, :status, :fattet, :fraDato, :tilDato)
                        ON CONFLICT DO NOTHING
                    """.trimMargin(),
                    mapOf(
                        "fnr" to fnr,
                        "vedtakId" to vedtakId,
                        "fagsakId" to fagsakId,
                        "status" to status.toString(),
                        "fattet" to datoFattet,
                        "fraDato" to fraDato,
                        "tilDato" to tilDato
                    )
                )
            )
        }
    }

    override fun hentSøknaderFor(fnr: String) =
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    "SELECT * FROM søknad WHERE person_id = (SELECT person_id FROM person WHERE fnr = ?)", fnr
                ).map { row -> row.toSøknad() }.asList
            )
        }

    override fun hentVedtakFor(
        fnr: String,
        fomFraDato: LocalDate?,
        tomFraDato: LocalDate?,
        fomTilDato: LocalDate?,
        tomTilDato: LocalDate?,
        status: List<Vedtak.Status>,
        offset: Int,
        limit: Int
    ): List<Vedtak> {
        val paramMap = mutableMapOf<String, Any>(
            "fnr" to fnr,
        )
        val where = mutableListOf<String>().apply {
            fomFraDato?.let {
                add("dato_innsendt::date >= :fom")
                paramMap["fom"] = it
            }
            tomFraDato?.let {
                add("dato_innsendt::date <= :tom")
                paramMap["tom"] = it
            }
            if (status.isNotEmpty()) {
                add("status IN (:status)")
                paramMap["status"] = status.joinToString(separator = "','", prefix = "'", postfix = "'")
            }
        }.joinToString(prefix = " AND ", separator = " AND ")

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    """SELECT *
                    FROM vedtak
                    WHERE person_id = (SELECT person_id FROM person WHERE fnr = :fnr) $where
                    ORDER BY dato_innsendt DESC
                    LIMIT $limit OFFSET $offset
                    """.trimIndent(),
                    paramMap
                ).map { row -> row.toVedtak() }.asList
            )
        }
    }

    override fun hentVedtakFor(fnr: String) =
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    "SELECT * FROM vedtak WHERE person_id = (SELECT person_id FROM person WHERE fnr = ?)", fnr
                ).map { row -> row.toVedtak() }.asList
            )
        }

    override fun hentSøknaderFor(
        fnr: String,
        fom: LocalDate?,
        tom: LocalDate?,
        type: List<SøknadsType>,
        kanal: List<Kanal>,
        offset: Int,
        limit: Int
    ): List<Søknad> {
        val paramMap = mutableMapOf<String, Any>(
            "fnr" to fnr,
        )
        val where = mutableListOf<String>().apply {
            fom?.let {
                add("dato_innsendt::date >= :fom")
                paramMap["fom"] = it
            }
            tom?.let {
                add("dato_innsendt::date <= :tom")
                paramMap["tom"] = it
            }
            if (type.isNotEmpty()) {
                add("søknads_type IN (:type)")
                paramMap["type"] = type.joinToString(separator = "','", prefix = "'", postfix = "'")
            }
        }.takeIf { it.isNotEmpty() }?.joinToString(prefix = " AND ", separator = " AND ") ?: ""

        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    """SELECT *
                    FROM søknad
                    WHERE person_id = (SELECT person_id FROM person WHERE fnr = :fnr) $where
                    ORDER BY dato_innsendt DESC
                    LIMIT $limit OFFSET $offset
                    """.trimIndent(),
                    paramMap
                ).map { row -> row.toSøknad() }.asList
            )
        }
    }

    private fun Row.toSøknad() = Søknad(
        søknadId = string("søknad_id"),
        journalpostId = string("journalpost_id"),
        skjemaKode = string("skjema_kode"),
        søknadsType = SøknadsType.valueOf(string("søknads_type")),
        kanal = Kanal.valueOf(string("kanal")),
        datoInnsendt = localDateTime("dato_innsendt")
    )

    private fun Row.toVedtak() = Vedtak(
        vedtakId = string("vedtak_id"),
        fagsakId = string("fagsak_id"),
        status = Vedtak.Status.valueOf(string("status")),
        datoFattet = localDateTime("fattet"),
        fraDato = localDateTime("fra_dato"),
        tilDato = localDateTimeOrNull("til_dato"),
    )
}
