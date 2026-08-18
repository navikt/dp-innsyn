# Aktiv dagpenger via dp-datadeling — moderniseringsguardrails

## Mål

- Bruk `dp-datadeling` som source of truth for signalet «aktiv dagpenger».
- Unngå ny akkumulering av legacy-logikk i `dp-innsyn`.

## Guardrails i denne runden

- Ny flyt bruker kun `GET /aktiv-dagpenger` (on-demand mot `dp-datadeling`).
- Oppslag mot `dp-datadeling` går til `POST /dagpenger/datadeling/v1/perioder`.
- Legacy-flyt (`/behandlingsstatus`) beholdes uendret for bakoverkompatibilitet. Ingen beslutning er tatt om videre bruk eller utfasing.
- Ved feil mot `dp-datadeling` returneres `harAktivDagpengerett=false` (fail-closed).
- Aktiv rett beregnes kun ut fra rettighetsperioder som overlapper dagens dato.
- Ingen ny persistering av aktiv-status i lokal database.

## Exit-kriterier for senere opprydding

- Frontend bruker kun `/aktiv-dagpenger` for dette signalet.
- Feilrate og latens mot `dp-datadeling` er stabil over avtalt periode.
- Ingen konsumenter er avhengige av legacy-heuristikk for aktiv dagpenger.
