CREATE INDEX vedtak_fattet_gist_idx ON vedtak USING gist (fattet);
CREATE INDEX soknad_dato_innsendt_gist_idx ON søknad USING gist (dato_innsendt);