package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak

class PersonData(
    private val fnr: String,
    private val søknader: List<Søknad>,
    private val vedtak: List<Vedtak>,
) {
    val person: Person
        get() =
            Person(fnr).also { p ->
                søknader.forEach { p.håndter(it) }
                vedtak.forEach { p.håndter(it) }
            }
}
