package no.nav.dagpenger.innsyn.modell.hendelser

class Mangelbrev(val id: String, val søknadId: String, oppgaver: List<Oppgave>): Hendelse(oppgaver) {

}
