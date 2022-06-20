ALTER TABLE vedtak
    ADD COLUMN fattet_dato DATE GENERATED ALWAYS AS ( date(fattet) ) STORED;

CREATE INDEX vedtak_fattet_gist_idx ON vedtak (fattet_dato);

ALTER TABLE søknad
    ADD COLUMN dato_innsendt_dato DATE GENERATED ALWAYS AS ( date(dato_innsendt) ) STORED;

CREATE INDEX soknad_dato_innsendt_gist_idx ON søknad (dato_innsendt_dato);