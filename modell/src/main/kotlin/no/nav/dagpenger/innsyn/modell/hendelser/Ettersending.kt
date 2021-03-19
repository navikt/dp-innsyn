package no.nav.dagpenger.innsyn.modell.hendelser

class Ettersending(
    val søknadId: String,
    val oppgaver: List<Oppgave>
) : Innsending()
