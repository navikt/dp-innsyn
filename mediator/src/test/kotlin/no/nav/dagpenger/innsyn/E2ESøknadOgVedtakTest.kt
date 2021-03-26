package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.Dagpenger.vedtakOppgave
import no.nav.dagpenger.innsyn.db.PostgresPersonRepository
import no.nav.dagpenger.innsyn.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.innsyn.modell.PersonJsonBuilder
import no.nav.dagpenger.innsyn.tjenester.EttersendingMottak
import no.nav.dagpenger.innsyn.tjenester.SøknadMottak
import no.nav.dagpenger.innsyn.tjenester.VedtakMottak
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class E2ESøknadOgVedtakTest {
    private val rapid = TestRapid()
    private val personRepository = PostgresPersonRepository()
    private val personMediator = PersonMediator(personRepository)
    private val søknadAsJson = javaClass.getResource("/søknadsinnsending.json").readText()
    private val ettersendingAsJson = javaClass.getResource("/ettersending.json").readText()
    private val vedtakAsJson = javaClass.getResource("/vedtak.json").readText()

    init {
        SøknadMottak(rapid, personMediator)
        EttersendingMottak(rapid, personMediator)
        VedtakMottak(rapid, personMediator)
    }

    @Test
    fun `skal kunne motta søknad og vedtak`() {
        withMigratedDb {
            rapid.sendTestMessage(søknadAsJson)
            assertTrue(person.harUferdigeOppgaverAv(vedtakOppgave))

            rapid.sendTestMessage(ettersendingAsJson)
            assertTrue(person.harUferdigeOppgaverAv(vedtakOppgave))

            rapid.sendTestMessage(vedtakAsJson)
            assertFalse(person.harUferdigeOppgaverAv(vedtakOppgave))

            println(PersonJsonBuilder(person).resultat().toString())
        }
    }

    private val person get() = personRepository.person("10108099999")
}
