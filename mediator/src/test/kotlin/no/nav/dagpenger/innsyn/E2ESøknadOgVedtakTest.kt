package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.db.PostgresPersonRepository
import no.nav.dagpenger.innsyn.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.innsyn.modell.EksternId
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.ProsessId
import no.nav.dagpenger.innsyn.modell.Søknadsprosess
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.Companion.søknadOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.Companion.vedleggOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.Companion.vedtakOppgave
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor
import no.nav.dagpenger.innsyn.modell.serde.SøknadListeJsonBuilder
import no.nav.dagpenger.innsyn.tjenester.EttersendingMottak
import no.nav.dagpenger.innsyn.tjenester.JournalførtMottak
import no.nav.dagpenger.innsyn.tjenester.PapirSøknadMottak
import no.nav.dagpenger.innsyn.tjenester.SøknadMottak
import no.nav.dagpenger.innsyn.tjenester.VedtakMottak
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class E2ESøknadOgVedtakTest {
    private val rapid = TestRapid()
    private val personRepository = PostgresPersonRepository()
    private val personMediator = PersonMediator(personRepository)
    private val søknadAsJson by lazy { javaClass.getResource("/søknad_mottatt.json").readText() }
    private val papirsøknadAsJson by lazy { javaClass.getResource("/papirsøknad_mottatt.json").readText() }
    private val journalførtAsJson by lazy { javaClass.getResource("/journalført.json").readText() }
    private val ettersendingAsJson by lazy { javaClass.getResource("/ettersending.json").readText() }
    private val vedtakAsJson by lazy { javaClass.getResource("/vedtak.json").readText() }

    init {
        SøknadMottak(rapid, personMediator)
        PapirSøknadMottak(rapid, personMediator)
        JournalførtMottak(rapid, personMediator)
        EttersendingMottak(rapid, personMediator)
        VedtakMottak(rapid, personMediator)
    }

    @Test
    fun `skal kunne motta søknad og vedtak`() {
        withMigratedDb {
            rapid.sendTestMessage(søknadAsJson)
            with(PersonInspektør(person)) {
                assertEquals(2, eksterneIder.size)
                assertEquals(1, stønadsforhold)
                assertEquals(0, vedtakOppgaver)
                assertEquals(2, uferdigeOppgaver)
                assertEquals(1, ferdigeOppgaver)
            }

            rapid.sendTestMessage(journalførtAsJson)
            with(PersonInspektør(person)) {
                assertEquals(3, eksterneIder.size)
                assertEquals(1, vedtakOppgaver)
                assertEquals(3, uferdigeOppgaver)
            }

            rapid.sendTestMessage(ettersendingAsJson)
            with(PersonInspektør(person)) {
                assertEquals(1, vedtakOppgaver)
                assertEquals(2, vedleggOppgaver)
                assertEquals(2, uferdigeOppgaver)
                assertEquals(2, ferdigeOppgaver)
            }

            rapid.sendTestMessage(vedtakAsJson)
            with(PersonInspektør(person)) {
                assertEquals(1, stønadsforhold)
                assertEquals(1, vedtakOppgaver)
                assertEquals(1, søknadOppgaver)
                assertEquals(2, vedleggOppgaver)
                assertEquals(1, uferdigeOppgaver)
                assertEquals(3, ferdigeOppgaver)
            }

            println(SøknadListeJsonBuilder(person).resultat().toPrettyString())
        }
    }

    @Test
    fun `skal kunne motta papirsøknad`() {
        withMigratedDb {
            rapid.sendTestMessage(papirsøknadAsJson)
            with(PersonInspektør(person)) {
                assertEquals(2, eksterneIder.size)
                assertEquals(1, stønadsforhold)
                assertEquals(0, vedtakOppgaver)
                assertEquals(0, uferdigeOppgaver)
                assertEquals(1, ferdigeOppgaver)
            }
        }
    }

    @Test
    fun `2 søknader skal gi 2 stønadsforhold`() {
        withMigratedDb {
            rapid.sendTestMessage(søknadsJson("123"))
            rapid.sendTestMessage(søknadsJson("456"))
            with(PersonInspektør(person)) {
                assertEquals(2, stønadsforhold)
            }
        }
    }

    @Test
    fun `vedtak uten søknad først`() {
        withMigratedDb {
            rapid.sendTestMessage(vedtakAsJson)
            with(PersonInspektør(person)) {
                assertEquals(0, stønadsforhold)
            }
        }
    }

    private val person get() = personRepository.person("10108099999")

    private class PersonInspektør(person: Person) : PersonVisitor {
        val eksterneIder = mutableSetOf<EksternId>()
        var uferdigeOppgaver = 0
        var ferdigeOppgaver = 0
        var søknadOppgaver = 0
        var vedleggOppgaver = 0
        var vedtakOppgaver = 0
        var stønadsforhold = 0

        init {
            person.accept(this)
        }

        override fun preVisit(
            søknadsprosess: Søknadsprosess,
            tilstand: Søknadsprosess.Tilstand
        ) {
            this.stønadsforhold++
        }

        override fun preVisit(stønadsid: ProsessId, internId: UUID, eksternId: EksternId) {
            eksterneIder.add(eksternId)
        }

        override fun preVisit(
            oppgave: Oppgave,
            id: Oppgave.OppgaveId,
            beskrivelse: String,
            opprettet: LocalDateTime,
            tilstand: Oppgave.OppgaveTilstand
        ) {
            when (tilstand) {
                Oppgave.OppgaveTilstand.Uferdig -> uferdigeOppgaver++
                Oppgave.OppgaveTilstand.Ferdig -> ferdigeOppgaver++
            }

            when (id.type) {
                søknadOppgave -> søknadOppgaver++
                vedleggOppgave -> vedleggOppgaver++
                vedtakOppgave -> vedtakOppgaver++
            }
        }
    }
}

@Language("JSON")
private fun søknadsJson(søknadsId: String) = """{
  "@id": "98638d1d-9b75-4802-abb2-8b7f1a08948f",
  "@opprettet": "2021-05-06T09:39:03.638555",
  "journalpostId": "12455",
  "datoRegistrert": "2021-05-06T09:39:03.62863",
  "type": "NySøknad",
  "fødselsnummer": "10108099999",
  "aktørId": "1234455",
    "søknadsData": {
    "brukerBehandlingId": $søknadsId,
    "aktoerId": "10108099999"
  },
  "@event_name": "innsending_mottatt",
  "system_read_count": 0
}"""
