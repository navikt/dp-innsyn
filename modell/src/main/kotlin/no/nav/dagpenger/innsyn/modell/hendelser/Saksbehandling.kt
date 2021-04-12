package no.nav.dagpenger.innsyn.modell.hendelser

class Saksbehandling(val id: String, val søknadId: String, oppgaver: Set<Oppgave>) : Hendelse(søknadId, oppgaver)
