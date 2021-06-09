CREATE TABLE IF NOT EXISTS person
(
    person_id BIGSERIAL PRIMARY KEY,
    fnr       CHAR(11) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS person_fnr_uindex ON person (fnr);

CREATE TABLE IF NOT EXISTS søknad(
    id              BIGSERIAL PRIMARY KEY,
    person_id       BIGSERIAL REFERENCES person,
    søknad_id       VARCHAR(12),
    journalpost_id   VARCHAR(255),
    skjema_kode     VARCHAR(20),
    søknads_type     VARCHAR(20) NOT NULL,
    kanal           VARCHAR(20) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS journalpost_id_uindex ON søknad(journalpost_id);

CREATE TABLE IF NOT EXISTS vedtak(
     id              BIGSERIAL PRIMARY KEY,
     person_id       BIGSERIAL REFERENCES person,
     vedtak_id       VARCHAR(12) NOT NULL,
     fagsak_id      VARCHAR(12) NOT NULL,
     status     VARCHAR(20) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS vedtak_id_uindex ON vedtak(vedtak_id)
