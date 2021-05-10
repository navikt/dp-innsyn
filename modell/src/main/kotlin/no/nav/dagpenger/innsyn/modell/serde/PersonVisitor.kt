package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.EksternId
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.ProsessId
import no.nav.dagpenger.innsyn.modell.Søknadsprosess
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import java.time.LocalDateTime
import java.util.UUID

interface PersonVisitor : SøknadsprosessVisitor {
    fun preVisit(person: Person, fnr: String) {}
    fun postVisit(person: Person, fnr: String) {}
}

interface SøknadsprosessVisitor : OppgaveVisitor, ProsessIdVisitor {
    fun preVisit(
        søknadsprosess: Søknadsprosess,
        tilstand: Søknadsprosess.Tilstand
    ) {}
    fun postVisit(
        søknadsprosess: Søknadsprosess,
        tilstand: Søknadsprosess.Tilstand
    ) {}
}

interface ProsessIdVisitor {
    fun preVisit(
        stønadsid: ProsessId,
        internId: UUID,
        eksternId: EksternId
    ) {}
    fun postVisit(
        stønadsid: ProsessId,
        internId: UUID,
        eksternId: EksternId
    ) {}
}

interface OppgaveVisitor {
    fun preVisit(
        oppgave: Oppgave,
        id: Oppgave.OppgaveId,
        beskrivelse: String,
        opprettet: LocalDateTime,
        tilstand: OppgaveTilstand
    ) {}
    fun postVisit(
        oppgave: Oppgave,
        id: Oppgave.OppgaveId,
        beskrivelse: String,
        opprettet: LocalDateTime,
        tilstand: OppgaveTilstand
    ) {}
}
