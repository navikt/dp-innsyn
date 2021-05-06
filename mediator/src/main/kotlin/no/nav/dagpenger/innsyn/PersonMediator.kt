package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.melding.Ettersendingsmelding
import no.nav.dagpenger.innsyn.melding.Hendelsemelding
import no.nav.dagpenger.innsyn.melding.Journalførtmelding
import no.nav.dagpenger.innsyn.melding.Søknadsmelding
import no.nav.dagpenger.innsyn.melding.Vedtaksmelding
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Hendelse
import no.nav.dagpenger.innsyn.modell.hendelser.Journalføring
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak

internal class PersonMediator(private val personRepository: PersonRepository) {
    fun håndter(søknad: Søknad, melding: Søknadsmelding) {
        håndter(søknad, melding) { person ->
            person.håndter(søknad)
        }
    }

    fun håndter(journalføring: Journalføring, melding: Journalførtmelding) {
        håndter(journalføring, melding) { person ->
            person.håndter(journalføring)
        }
    }

    fun håndter(vedtak: Vedtak, melding: Vedtaksmelding) {
        håndter(vedtak, melding) { person ->
            person.håndter(vedtak)
        }
    }

    fun håndter(ettersending: Ettersending, melding: Ettersendingsmelding) {
        håndter(ettersending, melding) { person ->
            person.håndter(ettersending)
        }
    }

    private fun håndter(hendelse: Hendelse, melding: Hendelsemelding, handler: (Person) -> Unit) {
        personRepository.person(melding.fødselsnummer).also { person ->
            handler(person)
            personRepository.lagre(person)
        }
    }
}
