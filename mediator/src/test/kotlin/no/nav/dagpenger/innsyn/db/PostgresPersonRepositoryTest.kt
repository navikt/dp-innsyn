package no.nav.dagpenger.innsyn.db

import no.nav.dagpenger.innsyn.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.innsyn.modell.Søknadsprosess
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PostgresPersonRepositoryTest {
    private val repository = PostgresPersonRepository()

    @Test
    fun `skal lagre og finne person`() {
        withMigratedDb {
            val person = repository.person("123")

            person.håndter(Søknadsprosess("id", listOf()))
            repository.lagre(person)

            repository.person(person.fnr).also {
                assertTrue(it.harSøknadUnderBehandling())
            }
        }
    }
}
