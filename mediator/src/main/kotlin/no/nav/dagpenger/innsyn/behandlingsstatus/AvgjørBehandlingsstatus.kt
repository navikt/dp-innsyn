package no.nav.dagpenger.innsyn.behandlingsstatus

import no.nav.dagpenger.innsyn.db.PersonRepository
import java.time.LocalDate

class AvgjørBehandlingsstatus(private val personRepository: PersonRepository) {

    fun hentStatus(fnr: String, fra: LocalDate, tom: LocalDate = LocalDate.now()): Behandlingsstatus {
        val antallSøknader = personRepository.hentSøknaderFor(fnr, fra, tom).size
        val antallVedtak = personRepository.hentVedtakFor(fnr, fra, tom).size
        return Behandlingsstatus(antallSøknader, antallVedtak)
    }
}
