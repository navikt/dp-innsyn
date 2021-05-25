package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType.Companion.søknadOppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.serde.SøknadListeJsonBuilder
import no.nav.dagpenger.innsyn.modell.serde.SøknadsprosessJsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDateTime
import java.util.UUID

class SøknadprosessJsonBuilderTest {
    @Test
    fun `vi kan bygge json`() {
        val person = Person("123")
        person.håndter(Søknad("1", "2", setOf(søknadOppgave.ny("ny", ""))))
        val internId = UUID.fromString(SøknadListeJsonBuilder(person).resultat().first()["id"].asText())
        val json = SøknadsprosessJsonBuilder(person, internId).resultat()

        assertDoesNotThrow { UUID.fromString(json["id"].asText()) }
        assertDoesNotThrow { LocalDateTime.parse(json["søknadstidspunkt"].asText()) }

        assertEquals(1, json["oppgaver"].size())
    }

    @Test
    fun `skal ikke ta med andre søknader enn den det gjelder`() {
        val person = Person("123")
        person.håndter(Søknad("1", "2", setOf(søknadOppgave.ny("ny", ""))))
        person.håndter(Søknad("3", "4", setOf(søknadOppgave.ny("ny", ""))))

        val internId = UUID.fromString(SøknadListeJsonBuilder(person).resultat().first()["id"].asText())
        val json = SøknadsprosessJsonBuilder(person, internId).resultat()

        assertEquals(1, json["oppgaver"].size())
    }

    @Test
    fun `skal fungere for søknad nr 2 også`() {
        val person = Person("123")
        person.håndter(Søknad("1", "2", setOf(søknadOppgave.ny("ny", ""))))
        person.håndter(Søknad("3", "4", setOf(søknadOppgave.ny("ny", ""))))

        val internId = UUID.fromString(SøknadListeJsonBuilder(person).resultat().last()["id"].asText())
        val json = SøknadsprosessJsonBuilder(person, internId).resultat()

        assertEquals(1, json["oppgaver"].size())
    }
}
