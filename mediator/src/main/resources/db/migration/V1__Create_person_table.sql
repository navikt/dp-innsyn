CREATE TABLE IF NOT EXISTS person
(
    person_id BIGSERIAL,
    fnr       CHAR(11) NOT NULL,
    PRIMARY KEY (person_id)
);
CREATE UNIQUE INDEX IF NOT EXISTS person_fnr_uindex ON person (fnr);

CREATE TABLE IF NOT EXISTS stønadsforhold
(
    id         BIGSERIAL NOT NULL,
    intern_id VARCHAR(255) NOT NULL,
    person_id  BIGINT NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS stønadsforhold_person_uindex ON stønadsforhold(intern_id, person_id);

CREATE TABLE IF NOT EXISTS oppgave
(
    oppgave_id  BIGSERIAL,
    stønadsforhold_id BIGINT NOT NULL,
    id          VARCHAR(255) NOT NULL,
    beskrivelse VARCHAR(255),
    opprettet   TIMESTAMP    NOT NULL,
    type        VARCHAR(255) NOT NULL,
    tilstand    VARCHAR(255) NOT NULL,
    PRIMARY KEY (oppgave_id)
);

CREATE TABLE IF NOT EXISTS eksternid(
    id BIGSERIAL,
    intern_id BIGINT NOT NULL,
    ekstern_id VARCHAR(255) NOT NULL,
    CONSTRAINT fk_stønadsid
        FOREIGN KEY (intern_id)
            REFERENCES stønadsforhold (id)
);