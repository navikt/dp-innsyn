package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.melding.Ettersendingsmelding
import no.nav.dagpenger.innsyn.melding.Søknadsmelding
import no.nav.dagpenger.innsyn.melding.Vedtaksmelding
import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak

internal class PersonMediator(private val personRepository: PersonRepository) {
    fun håndter(søknad: Søknad, melding: Søknadsmelding) {
        personRepository.person(melding.fødselsnummer).håndter(søknad)
    }

    fun håndter(søknad: Vedtak, melding: Vedtaksmelding) {
        personRepository.person(melding.fødselsnummer).håndter(søknad)
    }

    fun håndter(ettersending: Ettersending, melding: Ettersendingsmelding) {
        personRepository.person(melding.fødselsnummer).håndter(ettersending)
    }
}
