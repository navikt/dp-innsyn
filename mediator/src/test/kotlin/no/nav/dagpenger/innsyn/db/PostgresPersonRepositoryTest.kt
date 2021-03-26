package no.nav.dagpenger.innsyn.db

import no.nav.dagpenger.innsyn.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave.OppgaveType
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PostgresPersonRepositoryTest {
    private val repository = PostgresPersonRepository()

    @Test
    fun `skal lagre og finne person`() {
        withMigratedDb {
            val person = repository.person("123")
            val testOppgave = OppgaveType("test")

            person.håndter(Søknad("id", setOf(testOppgave.ny("oppgaver"))))
            repository.lagre(person)

            repository.person(person.fnr).also {
                assertTrue(it.harUferdigeOppgaverAv(testOppgave))
            }
        }
    }
}
