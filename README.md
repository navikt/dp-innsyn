# dp-innsyn

Gir deg svar på alt du lurer på

## API-notat

- `/aktiv-dagpenger` gjør on-demand oppslag mot `POST /dagpenger/datadeling/v1/perioder`.
- Responsen inneholder `harAktivDagpengerett: boolean`.

## Komme i gang

Gradle brukes som byggverktøy og er bundlet inn.

```
./gradlew build
```

## For å kjøre applikasjonen lokalt:

```
docker-compose up -d
./gradlew run
```

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

* André Roaldseth, andre.roaldseth@nav.no
* Eller en annen måte for omverden å kontakte teamet på

## For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen #dagpenger.
