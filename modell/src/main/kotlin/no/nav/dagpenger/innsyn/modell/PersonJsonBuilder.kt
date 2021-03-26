package no.nav.dagpenger.innsyn.modell

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PersonJsonBuilder(person: Person) : PersonVisitor {
    private val mapper = ObjectMapper()
    private val root: ObjectNode = mapper.createObjectNode()
    private val planNode = mapper.createArrayNode()

    init {
        person.accept(this)
    }

    fun resultat() = root

    override fun preVisit(person: Person, fnr: String) {
        // root.put("fnr", fnr)
        root.put("oppgaver", planNode)
    }

    override fun preVisit(
        oppgave: Oppgave,
        id: String,
        beskrivelse: String,
        opprettet: LocalDateTime,
        oppgaveType: Oppgave.OppgaveType,
        tilstand: OppgaveTilstand
    ) {
        planNode.addObject().also {
            it.put("id", id)
            it.put("beskrivelse", beskrivelse)
            it.put("opprettet", opprettet.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            it.put("oppgaveType", oppgaveType.toString())
            it.put("tilstand", tilstand.toString())
        }
    }
}
