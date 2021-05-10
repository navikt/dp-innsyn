package no.nav.dagpenger.innsyn.modell.serde

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.innsyn.modell.EksternId
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.ProsessId
import no.nav.dagpenger.innsyn.modell.Søknadsprosess
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveId
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import java.time.LocalDateTime
import java.util.UUID

class SøknadListeJsonBuilder(person: Person) : PersonVisitor {
    private lateinit var aktivProsess: UUID
    private val mapper = ObjectMapper()
    private val root = mapper.createArrayNode()
    private lateinit var prosess: ObjectNode

    init {
        person.accept(this)
    }

    fun resultat() = root

    override fun preVisit(søknadsprosess: Søknadsprosess, tilstand: Søknadsprosess.Tilstand) {
        prosess = mapper.createObjectNode()
    }

    override fun preVisit(stønadsid: ProsessId, internId: UUID, eksternId: EksternId) {
        prosess.put("id", internId.toString())
    }

    override fun preVisit(
        oppgave: Oppgave,
        id: OppgaveId,
        beskrivelse: String,
        opprettet: LocalDateTime,
        tilstand: OppgaveTilstand
    ) {
        if (id.type == Oppgave.OppgaveType.søknadOppgave) {
            prosess.put("søknadstidspunkt", opprettet.toString())
        }
    }

    override fun postVisit(søknadsprosess: Søknadsprosess, tilstand: Søknadsprosess.Tilstand) {
        root.add(prosess)
    }
}
