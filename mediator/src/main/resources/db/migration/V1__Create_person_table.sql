CREATE TABLE IF NOT EXISTS person
(
    person_id SERIAL,
    fnr       CHAR(11) NOT NULL,
    PRIMARY KEY (person_id)
);
CREATE UNIQUE INDEX IF NOT EXISTS person_fnr_uindex ON person (fnr);

CREATE TABLE søknad
(
    søknad_id  SERIAL,
    person_id  INT      NOT NULL,
    ekstern_id CHAR(20) NOT NULL,
    PRIMARY KEY (søknad_id),
    CONSTRAINT fk_person
        FOREIGN KEY (person_id)
            REFERENCES person (person_id)
);
