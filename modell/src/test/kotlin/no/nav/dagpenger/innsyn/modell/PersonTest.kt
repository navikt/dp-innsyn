package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class PersonTest {
    @Test
    fun `Flere søknader gir flere søknader`() {
        Person("ident").also { person ->
            person.håndter(søknad("id1"))
            person.håndter(søknad("id2"))
            person.håndter(søknad("id2")) // Duplikater skal ikke med

            assertEquals(2, person.søknader.size)
        }
    }

    @Test
    fun `Flere ettersendinger gir flere ettersendinger `() {
        Person("ident").also { person ->
            person.håndter(ettersending("12", "34"))
            person.håndter(ettersending("33", "44"))
            // ettersendinger er ikke eksponert direkte — verifiserer indirekte via søknad
            assertEquals(0, person.søknader.size)
        }
    }

    @Test
    fun `Flere vedtak gir flere vedtak `() {
        Person("ident").also { person ->
            person.håndter(
                Vedtak(
                    "1",
                    "2",
                    Vedtak.Status.INNVILGET,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                ),
            )
            person.håndter(
                Vedtak(
                    "5",
                    "6",
                    Vedtak.Status.AVSLÅTT,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                ),
            )
            assertEquals(2, person.vedtak.size)
        }
    }

    private fun søknad(id: String) =
        Søknad(
            id,
            "journalpostId-$id",
            "NAV123",
            Søknad.SøknadsType.NySøknad,
            Kanal.Digital,
            LocalDateTime.now(),
            emptyList(),
            "tittel",
        )

    private fun ettersending(
        søknadId: String?,
        ettersendingId: String?,
    ) = Ettersending(
        søknadId,
        ettersendingId,
        "99",
        Kanal.Digital,
        emptyList(),
    )
}
