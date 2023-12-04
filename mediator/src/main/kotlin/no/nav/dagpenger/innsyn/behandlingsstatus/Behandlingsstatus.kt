package no.nav.dagpenger.innsyn.behandlingsstatus

import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.FerdigBehandlet
import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.Ukjent
import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.UnderBehandling

class Behandlingsstatus(antallSøknader: Int, antallVedtak: Int) {
    internal var antattStatus: Status?

    init {
        antattStatus = status(antallSøknader, antallVedtak)
    }

    private fun status(
        antallSøknader: Int,
        antallVedtak: Int,
    ) = when {
        antallSøknader == 0 -> null
        antallSøknader > 0 && antallVedtak == 0 -> UnderBehandling
        antallSøknader > antallVedtak && antallVedtak > 0 -> Ukjent
        antallVedtak > 0 && antallSøknader > 0 -> FerdigBehandlet
        else -> null
    }

    enum class Status {
        Ukjent,
        FerdigBehandlet,
        UnderBehandling,
    }
}

data class BehandlingsstatusDTO(val behandlingsstatus: Behandlingsstatus.Status?)
