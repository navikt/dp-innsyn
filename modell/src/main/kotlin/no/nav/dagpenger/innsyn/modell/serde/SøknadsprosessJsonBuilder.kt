package no.nav.dagpenger.innsyn.modell.serde

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.innsyn.modell.EksternId
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.ProsessId
import no.nav.dagpenger.innsyn.modell.Søknadsprosess
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveId
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveTilstand.Ferdig
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.Companion.søknadOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.Companion.vedtakOppgave
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class SøknadsprosessJsonBuilder(person: Person, private val internId: UUID? = null) : PersonVisitor {
    private var ignore: Boolean = false
    private val mapper = ObjectMapper()
    private val root = mapper.createArrayNode()
    private lateinit var prosess: ObjectNode
    private lateinit var oppgaver: ArrayNode

    init {
        person.accept(this)
    }

    fun resultat() =
        if (internId != null) root.first()
        else root

    override fun preVisit(søknadsprosess: Søknadsprosess, tilstand: Søknadsprosess.Tilstand) {
        prosess = mapper.createObjectNode()
        oppgaver = mapper.createArrayNode()
    }

    override fun preVisit(stønadsid: ProsessId, internId: UUID, eksternId: EksternId) {
        prosess.put("id", internId.toString())
        if (this.internId != null) {
            ignore = internId != this.internId
        }
    }

    override fun preVisit(
        oppgave: Oppgave,
        id: OppgaveId,
        beskrivelse: String,
        opprettet: LocalDateTime,
        tilstand: OppgaveTilstand
    ) {
        if (id.type == søknadOppgave) {
            prosess.put("søknadstidspunkt", opprettet.toString())
        }
        if (id.type == vedtakOppgave && tilstand == Ferdig) {
            prosess.put("vedtakstidspunkt", opprettet.toString())
        }
        oppgaver.addObject().also {
            it.put("id", id.indeks)
            it.put("beskrivelse", beskrivelse)
            it.put("opprettet", opprettet.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            it.put("oppgaveType", id.type.toString())
            it.put("tilstand", tilstand.toString())
        }
    }

    override fun postVisit(søknadsprosess: Søknadsprosess, tilstand: Søknadsprosess.Tilstand) {
        if (ignore) return
        root.add(
            prosess.also {
                it.put("tilstand", tilstand.type.toString())
                it.put("oppgaver", oppgaver)
            }
        )
    }
}
