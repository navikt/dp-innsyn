CREATE TABLE IF NOT EXISTS person
(
    person_id BIGSERIAL PRIMARY KEY,
    fnr       CHAR(11) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS person_fnr_uindex ON person (fnr);

CREATE TABLE IF NOT EXISTS stønadsforhold
(
    stønadsforhold_id UUID PRIMARY KEY,
    person_id         BIGINT REFERENCES person,
    tilstand          VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS stønadsforhold_person_uindex ON stønadsforhold (stønadsforhold_id, person_id);

CREATE TABLE IF NOT EXISTS oppgave
(
    oppgave_id        BIGSERIAL PRIMARY KEY ,
    stønadsforhold_id UUID REFERENCES stønadsforhold,
    id                VARCHAR(255) NOT NULL,
    beskrivelse       VARCHAR(255),
    opprettet         TIMESTAMP    NOT NULL,
    type              VARCHAR(255) NOT NULL,
    tilstand          VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS oppgave_stønadsforhold_uindex ON oppgave (stønadsforhold_id, id, type);

CREATE TABLE IF NOT EXISTS stønadsid
(
    id                BIGSERIAL,
    stønadsforhold_id UUID REFERENCES stønadsforhold,
    ekstern_id        VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS stønadsid_uindex ON stønadsid (stønadsforhold_id, ekstern_id);
