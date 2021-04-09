package no.nav.dagpenger.innsyn.db

import no.nav.dagpenger.innsyn.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class PostgresPersonRepositoryTest {
    private val repository = PostgresPersonRepository()

    @Test
    fun `skal lagre og finne person`() {
        withMigratedDb {
            val person = repository.person("123")
            val testOppgave = OppgaveType("testType")

            person.håndter(Søknad("id", setOf(testOppgave.ny("oppgaver", "tom"))))
            repository.lagre(person)

            repository.person(person.fnr).also {
                with(PersonInspektør(it)) {
                    assertEquals(1, oppgaver)
                }
            }
        }
    }

    private class PersonInspektør(person: Person) : PersonVisitor {
        var oppgaver = 0

        init {
            person.accept(this)
        }

        override fun preVisit(
            oppgave: Oppgave,
            id: Oppgave.OppgaveId,
            beskrivelse: String,
            opprettet: LocalDateTime,
            tilstand: Oppgave.OppgaveTilstand
        ) {
            oppgaver++
        }
    }
}
