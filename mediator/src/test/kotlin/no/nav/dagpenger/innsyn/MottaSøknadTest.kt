package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.helpers.InMemoryPersonRepository
import no.nav.helse.rapids_rivers.testsupport.TestRapid
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
        rapid.sendTestMessage("""{"søknadId": 123, "fødselsnummer": "1234"}""")
        assertTrue(person.harSøknadUnderBehandling())
    }
}
