package no.nav.dagpenger.innsyn.db

import kotliquery.Row
import kotliquery.Session
import kotliquery.param
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending.Vedlegg
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending.Vedlegg.Status
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad.SøknadsType
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.serde.PersonData
import java.time.LocalDate
import java.time.LocalDateTime

class PostgresPersonRepository : PersonRepository {
    override fun person(fnr: String): Person = getPerson(fnr) ?: lagPerson(fnr)

    override fun lagre(person: Person): Boolean {
        sessionOf(dataSource).use { session ->
            session.transaction { tx ->
                tx.run(
                    queryOf(
                        //language=PostgreSQL
                        "INSERT INTO person (fnr) VALUES (:fnr) ON CONFLICT DO NOTHING",
                        mapOf("fnr" to person.fnr),
                    ).asUpdate,
                )

                val personId =
                    tx.run(
                        queryOf(
                            //language=PostgreSQL
                            "SELECT person_id FROM person WHERE fnr = ?",
                            person.fnr,
                        ).map { it.int(1) }.asSingle,
                    ) ?: error("person_id ikke funnet for fnr etter INSERT")

                if (person.søknader.isNotEmpty()) {
                    val søknadIder = person.søknader.mapNotNull { it.søknadId }
                    if (søknadIder.isNotEmpty()) {
                        tx.batchPreparedStatement(
                            //language=PostgreSQL
                            "DELETE FROM vedlegg WHERE søknad_id = ?",
                            søknadIder.map { listOf(it) },
                        )
                    }

                    tx.batchPreparedNamedStatement(
                        //language=PostgreSQL
                        """INSERT INTO søknad(person_id, søknad_id, journalpost_id, skjema_kode, søknads_type, kanal, dato_innsendt, tittel)
                            VALUES (:personId, :soknadId, :journalpostId, :skjemaKode, :soknadsType, :kanal, :datoInnsendt, :tittel)
                            ON CONFLICT DO NOTHING
                        """.trimMargin(),
                        person.søknader.map { s ->
                            mapOf(
                                "personId" to personId,
                                "soknadId" to s.søknadId,
                                "journalpostId" to s.journalpostId,
                                "skjemaKode" to s.skjemaKode,
                                "soknadsType" to s.søknadsType.toString(),
                                "kanal" to s.kanal.toString(),
                                "datoInnsendt" to s.datoInnsendt,
                                "tittel" to s.tittel,
                            )
                        },
                    )

                    val alleVedlegg =
                        person.søknader.flatMap { s ->
                            s.vedlegg.map { v ->
                                mapOf(
                                    "soknadId" to s.søknadId,
                                    "skjemaNummer" to v.skjemaNummer,
                                    "navn" to v.navn,
                                    "status" to v.status.toString(),
                                )
                            }
                        }
                    if (alleVedlegg.isNotEmpty()) {
                        tx.batchPreparedNamedStatement(
                            //language=PostgreSQL
                            """INSERT INTO vedlegg(søknad_id, skjema_nummer, navn, status)
                                VALUES (:soknadId, :skjemaNummer, :navn, :status)
                                ON CONFLICT DO NOTHING
                            """.trimMargin(),
                            alleVedlegg,
                        )
                    }
                }

                if (person.vedtak.isNotEmpty()) {
                    tx.batchPreparedNamedStatement(
                        //language=PostgreSQL
                        """INSERT INTO vedtak(person_id, vedtak_id, fagsak_id, status, fattet, fra_dato, til_dato)
                            VALUES (:personId, :vedtakId, :fagsakId, :status, :fattet, :fraDato, :tilDato)
                            ON CONFLICT DO NOTHING
                        """.trimMargin(),
                        person.vedtak.map { v ->
                            mapOf(
                                "personId" to personId,
                                "vedtakId" to v.vedtakId,
                                "fagsakId" to v.fagsakId,
                                "status" to v.status.toString(),
                                "fattet" to v.datoFattet,
                                "fraDato" to v.fraDato,
                                "tilDato" to v.tilDato,
                            )
                        },
                    )
                }
            }
        }
        return true
    }

    override fun lagreVedtak(
        fnr: String,
        vedtak: Vedtak,
    ) {
        sessionOf(dataSource).use { session ->
            session.transaction { tx ->
                tx.run(
                    queryOf(
                        //language=PostgreSQL
                        "INSERT INTO person (fnr) VALUES (:fnr) ON CONFLICT DO NOTHING",
                        mapOf("fnr" to fnr),
                    ).asUpdate,
                )
                tx.run(
                    queryOf(
                        //language=PostgreSQL
                        """INSERT INTO vedtak(person_id, vedtak_id, fagsak_id, status, fattet, fra_dato, til_dato)
                            VALUES ((SELECT person_id FROM person WHERE fnr = :fnr), :vedtakId, :fagsakId, :status, :fattet, :fraDato, :tilDato)
                            ON CONFLICT DO NOTHING
                        """.trimMargin(),
                        mapOf(
                            "fnr" to fnr,
                            "vedtakId" to vedtak.vedtakId,
                            "fagsakId" to vedtak.fagsakId,
                            "status" to vedtak.status.toString(),
                            "fattet" to vedtak.datoFattet,
                            "fraDato" to vedtak.fraDato,
                            "tilDato" to vedtak.tilDato,
                        ),
                    ).asUpdate,
                )
            }
        }
    }

    private fun lagPerson(fnr: String): Person = Person(fnr).also { lagre(it) }

    private fun getPerson(fnr: String) =
        sessionOf(dataSource).use { session ->
            session.run(selectPerson(fnr))?.let {
                val søknader = hentSøknaderFor(fnr)
                val vedtak = hentVedtakFor(session, it)
                PersonData(fnr, søknader, vedtak).person
            }
        }

    private fun hentVedtakFor(
        session: Session,
        personId: Int,
    ) = session.run(
        queryOf(
            //language=PostgreSQL
            "SELECT * FROM vedtak WHERE person_id = ?",
            personId,
        ).map { row ->
            Vedtak(
                vedtakId = row.string("vedtak_id"),
                fagsakId = row.string("fagsak_id"),
                status = Vedtak.Status.valueOf(row.string("status")),
                datoFattet = row.localDateTime("fattet"),
                fraDato = row.localDateTime("fra_dato"),
                tilDato = row.localDateTimeOrNull("til_dato"),
            )
        }.asList,
    )

    private fun selectPerson(fnr: String) =
        queryOf(
            //language=PostgreSQL
            "SELECT person_id FROM person WHERE fnr = ?",
            fnr,
        ).map { it.int(1) }.asSingle

    private data class SøknadRad(
        val id: Long,
        val søknadId: String?,
        val journalpostId: String,
        val skjemaKode: String,
        val søknadsType: SøknadsType,
        val kanal: Kanal,
        val datoInnsendt: LocalDateTime,
        val tittel: String?,
        val vedleggSkjemaNummer: String?,
        val vedleggNavn: String?,
        val vedleggStatus: String?,
    )

    private fun Row.toSøknadRad() =
        SøknadRad(
            id = long("id"),
            søknadId = stringOrNull("søknad_id"),
            journalpostId = string("journalpost_id"),
            skjemaKode = string("skjema_kode"),
            søknadsType = SøknadsType.valueOf(string("søknads_type")),
            kanal = Kanal.valueOf(string("kanal")),
            datoInnsendt = localDateTime("dato_innsendt"),
            tittel = stringOrNull("tittel"),
            vedleggSkjemaNummer = stringOrNull("skjema_nummer"),
            vedleggNavn = stringOrNull("vedlegg_navn"),
            vedleggStatus = stringOrNull("vedlegg_status"),
        )

    private fun List<SøknadRad>.groupSøknadRader(): List<Søknad> =
        this
            .groupBy { it.id }
            .values
            .map { rader ->
                val first = rader.first()
                val vedlegg =
                    rader.mapNotNull { rad ->
                        if (rad.vedleggStatus != null && rad.vedleggNavn != null && rad.vedleggSkjemaNummer != null) {
                            Vedlegg(
                                skjemaNummer = rad.vedleggSkjemaNummer,
                                navn = rad.vedleggNavn,
                                status = Status.valueOf(rad.vedleggStatus),
                            )
                        } else {
                            null
                        }
                    }
                Søknad(
                    søknadId = first.søknadId,
                    journalpostId = first.journalpostId,
                    skjemaKode = first.skjemaKode,
                    søknadsType = first.søknadsType,
                    kanal = first.kanal,
                    datoInnsendt = first.datoInnsendt,
                    vedlegg = vedlegg,
                    tittel = first.tittel,
                )
            }

    override fun hentSøknaderFor(fnr: String) =
        sessionOf(dataSource).use { session ->
            session
                .run(
                    queryOf(
                        //language=PostgreSQL
                        """
                        SELECT s.*, v.skjema_nummer, v.navn AS vedlegg_navn, v.status AS vedlegg_status
                        FROM søknad s
                        JOIN person p ON p.person_id = s.person_id
                        LEFT JOIN vedlegg v ON v.søknad_id = s.søknad_id
                        WHERE p.fnr = ?
                        ORDER BY s.dato_innsendt DESC, s.id
                        """.trimIndent(),
                        fnr,
                    ).map { row -> row.toSøknadRad() }.asList,
                ).groupSøknadRader()
        }

    override fun hentVedtakFor(fnr: String) =
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """
                    SELECT * FROM vedtak v
                    INNER JOIN person p ON p.person_id = v.person_id
                    WHERE p.fnr = ?
                    """.trimIndent(),
                    fnr,
                ).map { row -> row.toVedtak() }.asList,
            )
        }

    override fun hentVedtakFor(
        fnr: String,
        fattetFom: LocalDate?,
        fattetTom: LocalDate?,
        status: List<Vedtak.Status>,
        offset: Int,
        limit: Int,
    ): List<Vedtak> =
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """
                    SELECT *
                    FROM vedtak v,
                         person p
                    WHERE p.person_id = v.person_id
                      AND p.fnr = :fnr
                      AND v.fattet <@ TSRANGE(:fom, :tom) 
                    ORDER BY v.fattet DESC
                    LIMIT :limit OFFSET :offset
                    """.trimIndent(),
                    mapOf(
                        "fnr" to fnr,
                        "fom" to fattetFom,
                        "tom" to fattetTom?.plusDays(1),
                        "limit" to limit,
                        "offset" to offset,
                    ),
                ).map { row -> row.toVedtak() }.asList,
            )
        }

    override fun hentSøknaderFor(
        fnr: String,
        fom: LocalDate?,
        tom: LocalDate?,
        type: List<SøknadsType>,
        kanal: List<Kanal>,
        offset: Int,
        limit: Int,
    ) = sessionOf(dataSource).use { session ->
        session
            .run(
                queryOf(
                    //language=PostgreSQL
                    """
                    WITH paged_søknad AS (
                        SELECT s.*
                        FROM søknad s
                        JOIN person p ON p.person_id = s.person_id
                        WHERE p.fnr = :fnr
                          AND s.dato_innsendt <@ TSRANGE(:fom, :tom)
                        ORDER BY s.dato_innsendt DESC, s.id
                        LIMIT :limit OFFSET :offset
                    )
                    SELECT ps.*, v.skjema_nummer, v.navn AS vedlegg_navn, v.status AS vedlegg_status
                    FROM paged_søknad ps
                    LEFT JOIN vedlegg v ON v.søknad_id = ps.søknad_id
                    ORDER BY ps.dato_innsendt DESC, ps.id
                    """.trimIndent(),
                    mapOf(
                        "fnr" to fnr.param(),
                        "fom" to fom,
                        "tom" to tom?.plusDays(1),
                        "limit" to limit,
                        "offset" to offset,
                    ),
                ).map { row -> row.toSøknadRad() }.asList,
            ).groupSøknadRader()
    }

    private fun Row.toVedtak() =
        Vedtak(
            vedtakId = string("vedtak_id"),
            fagsakId = string("fagsak_id"),
            status = Vedtak.Status.valueOf(string("status")),
            datoFattet = localDateTime("fattet"),
            fraDato = localDateTime("fra_dato"),
            tilDato = localDateTimeOrNull("til_dato"),
        )
}
