CREATE TABLE IF NOT EXISTS person
(
    person_id BIGSERIAL PRIMARY KEY,
    fnr       CHAR(11) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS person_fnr_uindex ON person (fnr);

CREATE TABLE IF NOT EXISTS søknadsprosesser
(
    søknadsprosess_id uuid PRIMARY KEY,
    person_id         BIGINT REFERENCES person,
    tilstand          VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS søknadsprosess_person_uindex ON søknadsprosesser (søknadsprosess_id, person_id);

CREATE TABLE IF NOT EXISTS oppgave
(
    oppgave_id        BIGSERIAL PRIMARY KEY,
    søknadsprosess_id uuid REFERENCES søknadsprosesser,
    id                VARCHAR(255) NOT NULL,
    beskrivelse       VARCHAR(255),
    opprettet         TIMESTAMP    NOT NULL,
    type              VARCHAR(255) NOT NULL,
    tilstand          VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS oppgave_søknadsprosess_uindex ON oppgave (søknadsprosess_id, id, type);

CREATE TABLE IF NOT EXISTS prosess_id
(
    id                BIGSERIAL,
    søknadsprosess_id uuid REFERENCES søknadsprosesser,
    ekstern_type      VARCHAR(255) NOT NULL,
    ekstern_id        VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS prosess_id_uindex ON prosess_id (søknadsprosess_id, ekstern_type, ekstern_id);
