package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Hendelse
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand.Ferdig
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand.Uferdig
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor

class Person constructor(
    val fnr: String
) {
    private val behandlingskjeder = mutableSetOf<Behandlingskjede>()

    internal fun harUferdigeOppgaverAv(type: OppgaveType) = oppgaverAv(type, Uferdig).isNotEmpty()

    fun håndter(hendelse: Hendelse) {
        if (behandlingskjeder.map { it.håndter(hendelse) }.none { it }) {
            behandlingskjeder.add(hendelse.plan)
        }
    }

    private fun oppgaverAv(type: OppgaveType, oppgaveTilstand: Oppgave.OppgaveTilstand): Set<Oppgave> =
        mutableSetOf<Oppgave>().also { oppgaver ->
            behandlingskjeder.forEach { behandlingskjede ->
                behandlingskjede.forEach { oppgave ->
                    oppgave.leggTilHvis(
                        type,
                        oppgaveTilstand,
                        oppgaver
                    )
                }
            }
        }

    fun accept(visitor: PersonVisitor) {
        visitor.preVisit(this, fnr)
        behandlingskjeder.forEach { it.accept(visitor) }
        visitor.postVisit(this, fnr)
    }
}
