package no.nav.dagpenger.innsyn.modell.hendelser

class Journalføring(
    internal val journalpostId: String,
    internal val fagsakId: String,
    oppgaver: Set<Oppgave>
) : Hendelse(oppgaver)
