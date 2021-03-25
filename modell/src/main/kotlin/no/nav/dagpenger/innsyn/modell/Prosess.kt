package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Hendelse
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType

class Prosess constructor(
    private val fnr: String,
) {
    private var plan: Plan = Plan(emptyList())

    private fun ferdigeOppgaverAv(type: OppgaveType) =
        plan.filter { it.oppgaveType == type && it.tilstand == Oppgave.Ferdig }

    private fun uferdigeOppgaverAv(type: OppgaveType) =
        plan.filter { it.oppgaveType == type && it.tilstand == Oppgave.Uferdig }

    fun harUferdigeOppgaverAv(type: OppgaveType) = uferdigeOppgaverAv(type).isNotEmpty()
    fun harFerdigeOppgaverAv(type: OppgaveType) = ferdigeOppgaverAv(type).isNotEmpty()

    fun håndter(hendelse: Hendelse) {
        if (fnr != hendelse.prosessId) return

        plan = plan.slåSammen(hendelse.plan)
    }
}

internal class Plan(
    oppgaver: List<Oppgave>
) : Collection<Oppgave> by oppgaver {
    fun slåSammen(med: Plan): Plan {
        return this
    }
}
