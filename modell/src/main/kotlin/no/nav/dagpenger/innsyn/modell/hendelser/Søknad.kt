package no.nav.dagpenger.innsyn.modell.hendelser

class Søknad(
    val søknadId: String,
    val oppgaver: List<Oppgave>
) : Innsending()
