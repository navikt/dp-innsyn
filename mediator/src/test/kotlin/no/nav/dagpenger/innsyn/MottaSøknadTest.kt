package no.nav.dagpenger.innsyn

import no.nav.dagpenger.innsyn.helpers.InMemoryPersonRepository
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class MottaSøknadTest {
    private val rapid = TestRapid()
    private val personRepository = InMemoryPersonRepository()
    private val søknadAsJson = javaClass.getResource("/soknadsdata.json").readText()

    init {
        Søknadsmottak(rapid, personRepository)
    }

    @Test
    fun `skal kunne motta søknad`() {
        val person = personRepository.person("10108099999")
        rapid.sendTestMessage(søknadAsJson)
        assertTrue(person.harSøknadUnderBehandling())
    }
}
