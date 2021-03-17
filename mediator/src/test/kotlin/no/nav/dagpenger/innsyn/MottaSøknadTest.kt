package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.helpers.InMemoryPersonRepository
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class MottaSøknadTest {
    private val rapid = TestRapid()
    private val personRepository = InMemoryPersonRepository()

    init {
        Søknadsmottak(rapid, personRepository)
    }

    @Test
    fun `skal kunne motta søknad`() {
        val person = personRepository.person("1234")
        rapid.sendTestMessage(nySøknadJSON)
        assertTrue(person.harSøknadUnderBehandling())
    }
}

@Language("JSON")
private const val nySøknadJSON = """{
  "brukerBehandlingId": 123,
  "aktoerId": "1234",
  "journalpostId": "123"
}"""
