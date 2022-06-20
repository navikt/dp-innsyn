ALTER TABLE vedtak
    DROP COLUMN fattet_dato;
ALTER TABLE søknad
    DROP COLUMN dato_innsendt_dato;

DROP INDEX IF EXISTS vedtak_fattet_gist_idx ;
DROP INDEX IF EXISTS soknad_dato_innsendt_gist_idx;

CREATE EXTENSION IF NOT EXISTS btree_gist;
CREATE INDEX vedtak_fattet_gist_idx ON vedtak USING gist (fattet);
CREATE INDEX soknad_dato_innsendt_gist_idx ON søknad USING gist (dato_innsendt);