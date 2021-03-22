package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Mangelbrev
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak

class Person(
    val fnr: String,
    blæh: List<Søknadsprosess>
) {
    constructor(fnr: String) : this(fnr, listOf())

    private val blæh: MutableList<Søknadsprosess> = blæh.toMutableList()

    fun finnFerdigeOppgaverAv(type: OppgaveType) = blæh.flatMap { it.ferdigeOppgaverAv(type) }
    fun finnUferdigeOppgaverAv(type: OppgaveType) = blæh.flatMap { it.uferdigeOppgaverAv(type) }

    fun harUferdigeOppgaverAv(type: OppgaveType) = finnUferdigeOppgaverAv(type).isNotEmpty()
    fun harFerdigeOppgaverAv(type: OppgaveType) = finnFerdigeOppgaverAv(type).isNotEmpty()

    fun håndter(søknad: Søknad) {
        val søknadsprosess = Søknadsprosess(søknad.søknadId, søknad.oppgaver)
        blæh.add(søknadsprosess)
    }

    fun håndter(ettersending: Ettersending) {
        blæh.forEach { it.håndter(ettersending) }
    }

    fun håndter(vedtak: Vedtak) {
        blæh.forEach { it.håndter(vedtak) }
    }

    fun håndter(mangelbrev: Mangelbrev) {
        blæh.forEach { it.håndter(mangelbrev) }
    }
}
