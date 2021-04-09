CREATE TABLE IF NOT EXISTS person
(
    person_id BIGSERIAL,
    fnr       CHAR(11) NOT NULL,
    PRIMARY KEY (person_id)
);
CREATE UNIQUE INDEX IF NOT EXISTS person_fnr_uindex ON person (fnr);

CREATE TABLE IF NOT EXISTS behandlingskjede
(
    id         VARCHAR(255) NOT NULL,
    oppgave_id VARCHAR(255) NOT NULL,
    person_id  INT,
    PRIMARY KEY (id, oppgave_id, person_id),
    CONSTRAINT fk_person
        FOREIGN KEY (person_id)
            REFERENCES person (person_id)
);

CREATE TABLE oppgave
(
    oppgave_id  BIGSERIAL,
    person_id   INT,
    id          VARCHAR(255) NOT NULL,
    beskrivelse VARCHAR(255),
    opprettet   TIMESTAMP    NOT NULL,
    type        VARCHAR(255) NOT NULL,
    tilstand    VARCHAR(255) NOT NULL,
    PRIMARY KEY (oppgave_id),
    CONSTRAINT fk_person
        FOREIGN KEY (person_id)
            REFERENCES person (person_id)
);
CREATE UNIQUE INDEX IF NOT EXISTS oppgave_person_uindex ON oppgave (person_id, id, type);