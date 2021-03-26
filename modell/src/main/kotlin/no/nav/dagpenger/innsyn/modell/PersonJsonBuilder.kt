package no.nav.dagpenger.innsyn.modell

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand

class PersonJsonBuilder(person: Person) : PersonVisitor {
    private val mapper = ObjectMapper()
    private val root: ObjectNode = mapper.createObjectNode()
    private val planNode: ArrayNode = mapper.createArrayNode()

    init {
        person.accept(this)
    }

    fun resultat() = root

    override fun preVisit(person: Person, fnr: String) {
        // root.put("fnr", fnr)
        root.put("oppgaver", planNode)
    }

    override fun preVisit(oppgave: Oppgave, id: String, oppgaveType: Oppgave.OppgaveType, tilstand: OppgaveTilstand) {
        planNode.addObject().also {
            it.put("id", id)
            it.put("oppgaveType", oppgaveType.toString())
            it.put("tilstand", tilstand.toString())
        }
    }
}
