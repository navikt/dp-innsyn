package no.nav.dagpenger.innsyn.behandlingsstatus

import no.nav.dagpenger.innsyn.db.PersonRepository
import java.time.LocalDate

internal class AvgjørBehandlingsstatus(
    private val personRepository: PersonRepository,
) {
    internal fun hentStatus(
        fnr: String,
        fom: LocalDate?,
        tom: LocalDate = LocalDate.now(),
    ): Behandlingsstatus.Status? {
        val antallSøknader = personRepository.hentSøknaderFor(fnr, fom, tom).size
        val antallVedtak = personRepository.hentVedtakFor(fnr, fom, tom).size
        return Behandlingsstatus(antallSøknader, antallVedtak).antattStatus
    }
}
