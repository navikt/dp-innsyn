package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType
import org.junit.jupiter.api.Test

internal class PersonJsonBuilderTest {
    private val oppgaveType = OppgaveType("test")

    @Test
    fun `vi kan bygge json`() {
        /*val person = Person("123")
        person.håndter(Søknad("1", setOf(oppgaveType.ny("ny", "")), ""))

        val json = PersonJsonBuilder(person).resultat()

        assertEquals(1, json["oppgaver"].size())

        println(json)*/
    }
}
