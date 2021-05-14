package no.nav.dagpenger.innsyn.modell.hendelser

class Journalføring constructor(
    internal val journalpostId: String,
    internal val fagsakId: String,
    oppgaver: Set<Oppgave>
) : Hendelse(oppgaver) {
    constructor(journalpostId: String, fagsakId: String) : this(journalpostId, fagsakId, emptySet())
}
