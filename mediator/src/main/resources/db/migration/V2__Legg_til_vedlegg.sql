CREATE UNIQUE INDEX IF NOT EXISTS søknad_id_index ON søknad (søknad_id);

CREATE TABLE IF NOT EXISTS vedlegg
(
    id            BIGSERIAL PRIMARY KEY,
    søknad_id     VARCHAR(12) REFERENCES søknad(søknad_id),
    skjema_nummer VARCHAR(20),
    navn          VARCHAR(255) NOT NULL,
    status        VARCHAR(40)  NOT NULL
);
