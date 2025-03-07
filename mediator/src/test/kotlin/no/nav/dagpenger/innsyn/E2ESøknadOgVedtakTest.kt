package no.nav.dagpenger.innsyn

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import no.nav.dagpenger.innsyn.db.PostgresPersonRepository
import no.nav.dagpenger.innsyn.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor
import no.nav.dagpenger.innsyn.tjenester.EttersendingMottak
import no.nav.dagpenger.innsyn.tjenester.JournalførtMottak
import no.nav.dagpenger.innsyn.tjenester.SøknadMottak
import no.nav.dagpenger.innsyn.tjenester.VedtakMottak
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

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
        JournalførtMottak(rapid, personMediator)
        EttersendingMottak(rapid, personMediator)
        VedtakMottak(rapid, personMediator)
    }

    @Test
    fun `skal kunne motta flere søknader`() {
        withMigratedDb {
            rapid.sendTestMessage(søknadsJson("999", "søknad1"))
            with(PersonInspektør(person)) {
                assertEquals(1, søknader)
            }

            rapid.sendTestMessage(søknadsJson("123", "søknad2"))
            with(PersonInspektør(person)) {
                assertEquals(2, søknader)
            }
        }
    }

    @Test
    fun `skal kunne liste opp vedlegg og ettersende vedlegg`() {
        withMigratedDb {
            rapid.sendTestMessage(søknadAsJson)
            with(PersonInspektør(person)) {
                assertEquals(2, uferdigeVedlegg)
                assertEquals(0, ferdigeVedlegg)
            }

            rapid.sendTestMessage(ettersendingAsJson)
            with(PersonInspektør(person)) {
                assertEquals(1, uferdigeVedlegg)
                assertEquals(1, ferdigeVedlegg)
            }
        }
    }

    @Test
    fun `skal kunne motta flere vedtak`() {
        withMigratedDb {
            rapid.sendTestMessage(vedtakAsJson)
            with(PersonInspektør(person)) {
                assertEquals(1, vedtak)
            }
        }
    }

    private val person get() = personRepository.person("10108099999")

    private class PersonInspektør(
        person: Person,
    ) : PersonVisitor {
        var søknader = 0
        var ferdigeVedlegg = 0
        var uferdigeVedlegg = 0
        var vedtak = 0

        init {
            person.accept(this)
        }

        override fun visitSøknad(
            søknadId: String?,
            journalpostId: String,
            skjemaKode: String?,
            søknadsType: Søknad.SøknadsType,
            kanal: Kanal,
            datoInnsendt: LocalDateTime,
            tittel: String?,
        ) {
            søknader++
        }

        override fun visitVedlegg(
            skjemaNummer: String,
            navn: String,
            status: Innsending.Vedlegg.Status,
        ) {
            if (status == Innsending.Vedlegg.Status.LastetOpp) {
                ferdigeVedlegg++
            } else {
                uferdigeVedlegg++
            }
        }

        override fun visitVedtak(
            vedtakId: String,
            fagsakId: String,
            status: Vedtak.Status,
            datoFattet: LocalDateTime,
            fraDato: LocalDateTime,
            tilDato: LocalDateTime?,
        ) {
            vedtak++
        }
    }
}

@Language("JSON")
private fun søknadsJson(
    journalpostId: String,
    søknadsId: String,
) = """{
  "@id": "98638d1d-9b75-4802-abb2-8b7f1a08948f",
  "@opprettet": "2021-05-06T09:39:03.638555",
  "journalpostId": $journalpostId,
  "datoRegistrert": "2021-05-06T09:39:03.62863",
  "skjemaKode": "NAV 03-102.23",
  "tittel": "Tittel",
  "type": "NySøknad",
  "fødselsnummer": "10108099999",
  "aktørId": "1234455",
    "søknadsData": {
    "brukerBehandlingId": "$søknadsId",
    "skjemaNummer": "NAV123",
    "aktoerId": "10108099999"
  },
  "@event_name": "innsending_mottatt",
  "system_read_count": 0
}"""
