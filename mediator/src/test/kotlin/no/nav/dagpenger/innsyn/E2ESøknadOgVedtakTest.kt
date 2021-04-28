package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.Dagpenger.søknadOppgave
import no.nav.dagpenger.innsyn.Dagpenger.vedleggOppgave
import no.nav.dagpenger.innsyn.Dagpenger.vedtakOppgave
import no.nav.dagpenger.innsyn.db.PostgresPersonRepository
import no.nav.dagpenger.innsyn.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.Stønadsforhold
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.serde.PersonJsonBuilder
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor
import no.nav.dagpenger.innsyn.tjenester.EttersendingMottak
import no.nav.dagpenger.innsyn.tjenester.SøknadMottak
import no.nav.dagpenger.innsyn.tjenester.VedtakMottak
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class E2ESøknadOgVedtakTest {
    private val rapid = TestRapid()
    private val personRepository = PostgresPersonRepository()
    private val personMediator = PersonMediator(personRepository)
    private val søknadAsJson by lazy { javaClass.getResource("/søknadsinnsending.json").readText() }
    private val ettersendingAsJson by lazy { javaClass.getResource("/ettersending.json").readText() }
    private val vedtakAsJson by lazy { javaClass.getResource("/vedtak.json").readText() }

    init {
        SøknadMottak(rapid, personMediator)
        EttersendingMottak(rapid, personMediator)
        VedtakMottak(rapid, personMediator)
    }

    @Test
    fun `skal kunne motta søknad og vedtak`() {
        withMigratedDb {
            rapid.sendTestMessage(søknadAsJson)
            with(PersonInspektør(person)) {
                assertEquals(1, stønadsforhold)
                assertEquals(1, vedtakOppgaver)
                assertEquals(3, uferdigeOppgaver)
                assertEquals(1, ferdigeOppgaver)
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
                assertEquals(1, vedtakOppgaver)
                assertEquals(1, søknadOppgaver)
                assertEquals(2, vedleggOppgaver)
                assertEquals(1, uferdigeOppgaver)
                assertEquals(3, ferdigeOppgaver)
            }

            println(PersonJsonBuilder(person).resultat().toPrettyString())
        }
    }

    @Test
    fun `2 søknader skal gi 2 stønadsforhold`(){
        withMigratedDb {
            rapid.sendTestMessage(søknadsJson("123"))
            rapid.sendTestMessage(søknadsJson("456"))
            with(PersonInspektør(person)){
                assertEquals(2, stønadsforhold)
            }
        }
    }

    @Test
    fun `vedtak uten søknad først`(){
        withMigratedDb {
            rapid.sendTestMessage(vedtakAsJson)
            with(PersonInspektør(person)){
                assertEquals(0, stønadsforhold)
            }
        }
    }

    private val person get() = personRepository.person("10108099999")

    private class PersonInspektør(person: Person) : PersonVisitor {
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
            stønadsforhold: Stønadsforhold,
            tilstand: Stønadsforhold.Tilstand
        ) {
            this.stønadsforhold++
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
  "søknadsdata": {
    "brukerBehandlingId": $søknadsId,
    "aktoerId": "10108099999"
  },
  "journalpostId": "493355115",
  "henvendelsestype": "NY_SØKNAD",
  "aktørId": "1819645303073",
  "naturligIdent": "10108099999"
}"""
