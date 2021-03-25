package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Hendelse
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType

class Person constructor(
    val fnr: String,
) {
    private var plan: Plan = Plan(emptySet())

    private fun ferdigeOppgaverAv(type: OppgaveType) =
        plan.filter { it.oppgaveType == type && it.tilstand == Oppgave.Ferdig }

    private fun uferdigeOppgaverAv(type: OppgaveType) =
        plan.filter { it.oppgaveType == type && it.tilstand == Oppgave.Uferdig }

    fun harUferdigeOppgaverAv(type: OppgaveType) = uferdigeOppgaverAv(type).isNotEmpty()
    fun harFerdigeOppgaverAv(type: OppgaveType) = ferdigeOppgaverAv(type).isNotEmpty()

    fun håndter(hendelse: Hendelse) {
        plan = plan.slåSammen(hendelse.plan)
    }
}

internal class Plan(
    val oppgaver: Set<Oppgave>
) : Collection<Oppgave> by oppgaver {
    fun slåSammen(med: Plan): Plan {

        val unike = oppgaver subtract med.oppgaver

        return Plan(unike + med.oppgaver)
    }
}
