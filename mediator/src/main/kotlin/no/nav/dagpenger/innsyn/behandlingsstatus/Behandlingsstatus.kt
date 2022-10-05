package no.nav.dagpenger.innsyn.behandlingsstatus

import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.FerdigBehandlet
import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.UnderOgFerdigBehandlet
import no.nav.dagpenger.innsyn.behandlingsstatus.Behandlingsstatus.Status.UnderBehandling
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak

class Behandlingsstatus(vedtak: List<Vedtak>, søknader: List<Søknad>) {
    internal var status: Status?

    init {
        status = status(vedtak, søknader)
    }

    private fun status(vedtak: List<Vedtak>, søknader: List<Søknad>) =
        when {
            søknader.isEmpty() && vedtak.isNotEmpty() -> null
            søknader.size > vedtak.size && vedtak.isNotEmpty() -> UnderOgFerdigBehandlet
            vedtak.isNotEmpty() -> FerdigBehandlet
            søknader.isNotEmpty() -> UnderBehandling
            vedtak.isEmpty() || søknader.isEmpty() -> null
            else -> null
        }

    internal enum class Status {
        UnderOgFerdigBehandlet,
        FerdigBehandlet,
        UnderBehandling
    }
}