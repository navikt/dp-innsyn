package no.nav.dagpenger.innsyn.modell.hendelser

class PapirSøknad(
    internal val journalpostId: String,
    oppgaver: Set<Oppgave>
) : Innsending("EN ID", oppgaver)
