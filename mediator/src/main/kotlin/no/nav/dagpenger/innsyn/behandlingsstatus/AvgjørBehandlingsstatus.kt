package no.nav.dagpenger.innsyn.behandlingsstatus

import no.nav.dagpenger.innsyn.db.PersonRepository
import java.time.LocalDate

internal class AvgjørBehandlingsstatus(private val personRepository: PersonRepository) {

    private val innsynFom = LocalDate.now().minusDays(28)

    internal fun hentStatus(fnr: String, kvitteringFom: LocalDate?, tom: LocalDate = LocalDate.now()): Behandlingsstatus {
        val fom: LocalDate = kvitteringFom ?: innsynFom
        val antallSøknader = personRepository.hentSøknaderFor(fnr, fom, tom).size
        val antallVedtak = personRepository.hentVedtakFor(fnr, fom, tom).size
        return Behandlingsstatus(antallSøknader, antallVedtak)
    }
}
