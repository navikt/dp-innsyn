package no.nav.dagpenger.innsyn.modell.serde

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.innsyn.modell.EksternId
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.ProsessId
import no.nav.dagpenger.innsyn.modell.Søknadsprosess
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.Companion.søknadOppgave
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class SøknadsprosessJsonBuilder(person: Person, private val internId: UUID) : PersonVisitor {

    private lateinit var søknadstidspunkt: LocalDateTime
    private var ignore: Boolean = false
    private val mapper = ObjectMapper()
    private val root: ObjectNode = mapper.createObjectNode()
    private val oppgaver = mapper.createArrayNode()

    init {
        person.accept(this)
    }

    fun resultat() = root

    override fun preVisit(stønadsid: ProsessId, internId: UUID, eksternId: EksternId) {
        if (internId != this.internId) {
            ignore = true
        }
    }

    override fun preVisit(
        oppgave: Oppgave,
        id: Oppgave.OppgaveId,
        beskrivelse: String,
        opprettet: LocalDateTime,
        tilstand: Oppgave.OppgaveTilstand
    ) {
        if (ignore) return
        oppgaver.addObject().also {
            it.put("id", id.indeks)
            it.put("beskrivelse", beskrivelse)
            it.put("opprettet", opprettet.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            it.put("oppgaveType", id.type.toString())
            it.put("tilstand", tilstand.toString())
        }

        if (id.type == søknadOppgave) {
            søknadstidspunkt = opprettet
        }
    }

    override fun postVisit(søknadsprosess: Søknadsprosess, tilstand: Søknadsprosess.Tilstand) {
        root.put("id", internId.toString())
        root.put("søknadstidspunkt", søknadstidspunkt.toString())
        root.put("oppgaver", oppgaver)

        ignore = false
    }
}
