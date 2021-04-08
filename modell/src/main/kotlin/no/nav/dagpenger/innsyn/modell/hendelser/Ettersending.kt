package no.nav.dagpenger.innsyn.modell.hendelser

class Ettersending(
    val søknadId: String,
    oppgaver: Set<Oppgave>
) : Innsending(søknadId, oppgaver)
