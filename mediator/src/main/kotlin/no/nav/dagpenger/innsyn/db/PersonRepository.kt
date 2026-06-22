package no.nav.dagpenger.innsyn.db

import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak

interface PersonRepository :
    SøknadRepository,
    VedtakRepository {
    fun person(fnr: String): Person

    fun lagre(person: Person): Boolean

    fun lagreVedtak(
        fnr: String,
        vedtak: Vedtak,
    )
}
