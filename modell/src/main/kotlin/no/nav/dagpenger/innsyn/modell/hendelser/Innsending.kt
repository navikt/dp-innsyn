package no.nav.dagpenger.innsyn.modell.hendelser

abstract class Innsending(behandlingskjedeId: String, oppgaver: Set<Oppgave>) : Hendelse(
    oppgaver
)
