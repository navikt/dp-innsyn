package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Sakstilknytning
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad.Companion.har
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor

class Person private constructor(
    val fnr: String,
    private val søknader: MutableList<Søknad>,
    private val ettersendinger: MutableList<Ettersending>,
    private val vedtak: MutableList<Vedtak>,
    private val sakstilknytninger: MutableList<Sakstilknytning>
) {
    constructor(fnr: String) : this(fnr, mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())

    fun håndter(søknad: Søknad) {
        if (søknader.har(søknad)) return
        søknader.add(søknad)
    }

    fun håndter(ettersending: Ettersending) {
        søknader.forEach { it.håndter(ettersending) }
        ettersendinger.add(ettersending)
    }

    fun håndter(vedtak: Vedtak) {
        this.vedtak.add(vedtak)
    }

    fun håndter(sakstilknytning: Sakstilknytning) {
        sakstilknytninger.add(sakstilknytning)
    }

    fun accept(visitor: PersonVisitor) {
        visitor.preVisit(this, fnr)
        søknader.forEach { it.accept(visitor) }
        ettersendinger.forEach { it.accept(visitor) }
        vedtak.forEach { it.accept(visitor) }
        sakstilknytninger.forEach { it.accept(visitor) }
        visitor.postVisit(this, fnr)
    }
}
