package no.nav.dagpenger.innsyn.behandlingsstatus

import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.FerdigBehandlet
import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.UnderBehandling
import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.UnderOgFerdigBehandlet

class Behandlingsstatus(antallSøknader: Int, antallVedtak: Int) {
    internal var antattStatus: Status?

    init {
        antattStatus = status(antallSøknader, antallVedtak)
    }

    private fun status(antallSøknader: Int, antallVedtak: Int) =
        when {
            antallSøknader == 0 -> null
            antallSøknader > 0 && antallVedtak == 0 -> UnderBehandling
            antallSøknader > antallVedtak && antallVedtak > 0 -> UnderOgFerdigBehandlet
            antallVedtak > 0 && antallSøknader > 0 -> FerdigBehandlet
            else -> null
        }

    internal enum class Status {
        UnderOgFerdigBehandlet,
        FerdigBehandlet,
        UnderBehandling
    }
}
