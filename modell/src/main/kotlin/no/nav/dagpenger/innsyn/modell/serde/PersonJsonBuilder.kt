package no.nav.dagpenger.innsyn.modell.serde

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.ProsessId
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveId
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class PersonJsonBuilder(person: Person) : PersonVisitor {
    private val mapper = ObjectMapper()
    private val root: ObjectNode = mapper.createObjectNode()
    private val planNode = mapper.createArrayNode()
    private lateinit var stønadsforholdId: UUID

    init {
        person.accept(this)
    }

    fun resultat() = root

    override fun preVisit(person: Person, fnr: String) {
        // root.put("fnr", fnr)
        root.put("oppgaver", planNode)
    }

    override fun preVisit(stønadsid: ProsessId, internId: UUID, eksternId: String) {
        stønadsforholdId = internId
    }

    override fun preVisit(
        oppgave: Oppgave,
        id: OppgaveId,
        beskrivelse: String,
        opprettet: LocalDateTime,
        tilstand: OppgaveTilstand
    ) {
        planNode.addObject().also {
            it.put("id", id.indeks)
            it.put("stønadsforholdId", stønadsforholdId.toString())
            it.put("beskrivelse", beskrivelse)
            it.put("opprettet", opprettet.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            it.put("oppgaveType", id.type.toString())
            it.put("tilstand", tilstand.toString())
        }
    }
}
