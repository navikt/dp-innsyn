package no.nav.dagpenger.innsyn.modell.hendelser

class Søknad(
    val søknadId: String,
    oppgaver: Set<Oppgave>
) : Innsending(oppgaver)
