package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Hendelse
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor

class Person private constructor(
    val fnr: String,
    private val stønadsforhold: MutableSet<Stønadsforhold>
) {
    constructor(fnr: String) : this(fnr, mutableSetOf())

    fun håndter(hendelse: Hendelse) {
        if (stønadsforhold.map { it.håndter(hendelse) }.none { it }) {
            stønadsforhold.add(Stønadsforhold().also { it.håndter(hendelse) })
        }
    }

    fun accept(visitor: PersonVisitor) {
        visitor.preVisit(this, fnr)
        stønadsforhold.forEach { it.accept(visitor) }
        visitor.postVisit(this, fnr)
    }
}
