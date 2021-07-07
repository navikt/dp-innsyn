package no.nav.dagpenger.innsyn.db

import no.nav.dagpenger.innsyn.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending.Vedlegg
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending.Vedlegg.Status.LastetOpp
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
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

            person.håndter(
                Søknad(
                    "id",
                    "journalpostId",
                    "NAV01",
                    Søknad.SøknadsType.NySøknad,
                    Kanal.Digital,
                    LocalDateTime.now(),
                    listOf(
                        Vedlegg("123", "123", LastetOpp)
                    )
                )
            )
            repository.lagre(person)

            repository.person(person.fnr).also {
                with(PersonInspektør(it)) {
                    assertEquals(1, søknader)
                    assertEquals(1, vedlegg)
                }
            }
        }
    }

    private class PersonInspektør(person: Person) : PersonVisitor {
        var søknader = 0
        var vedlegg = 0

        init {
            person.accept(this)
        }

        override fun visitSøknad(
            søknadId: String?,
            journalpostId: String,
            skjemaKode: String?,
            søknadsType: Søknad.SøknadsType,
            kanal: Kanal,
            datoInnsendt: LocalDateTime
        ) {
            søknader++
        }

        override fun visitVedlegg(skjemaNummer: String, navn: String, status: Vedlegg.Status) {
            vedlegg++
        }
    }
}
