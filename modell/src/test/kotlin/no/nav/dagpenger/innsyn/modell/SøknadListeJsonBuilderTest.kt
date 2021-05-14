package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.Companion.søknadOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.serde.SøknadListeJsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDateTime
import java.util.UUID

internal class SøknadListeJsonBuilderTest {
    @Test
    fun `vi kan bygge json med flere søknader`() {
        val person = Person("123")
        person.håndter(Søknad("1", "11", setOf(søknadOppgave.ferdig("ferdig", ""))))
        person.håndter(Søknad("2", "22", setOf(søknadOppgave.ny("ny", ""))))
        val json = SøknadListeJsonBuilder(person).resultat()

        assertEquals(2, json.size())

        assertDoesNotThrow { UUID.fromString(json.first()["id"].asText()) }
        assertDoesNotThrow { LocalDateTime.parse(json.first()["søknadstidspunkt"].asText()) }

        assertDoesNotThrow { UUID.fromString(json.last()["id"].asText()) }
        assertDoesNotThrow { LocalDateTime.parse(json.last()["søknadstidspunkt"].asText()) }

        assertNotEquals(json.first(), json.last())
    }
}
