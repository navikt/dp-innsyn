package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Hendelse
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor

class Person private constructor(
    val fnr: String,
    private val søknadsprosesser: MutableSet<Søknadsprosess>
) {
    constructor(fnr: String) : this(fnr, mutableSetOf())

    fun håndter(hendelse: Hendelse) {
        if (søknadsprosesser.map { it.håndter(hendelse) }.none { it }) {
            if (hendelse is Søknad) søknadsprosesser.add(Søknadsprosess().also { it.håndter(hendelse) })
        }
    }

    fun accept(visitor: PersonVisitor) {
        visitor.preVisit(this, fnr)
        søknadsprosesser.forEach { it.accept(visitor) }
        visitor.postVisit(this, fnr)
    }
}
