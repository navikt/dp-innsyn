package no.nav.dagpenger.innsyn.modell

import no.nav.dagpenger.innsyn.modell.hendelser.Ettersending
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import no.nav.dagpenger.innsyn.modell.serde.PersonVisitor
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

            assertEquals(2, PersonInspektør(person).antallSøknader)
        }
    }

    @Test
    fun `Flere ettersendinger gir flere ettersendinger `() {
        Person("ident").also { person ->
            person.håndter(ettersending("12", "34"))
            person.håndter(ettersending("33", "44"))
            assertEquals(2, PersonInspektør(person).antallEttersendinger)
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
                    LocalDateTime.now()
                )
            )
            person.håndter(
                Vedtak(
                    "5",
                    "6",
                    Vedtak.Status.AVSLÅTT,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    LocalDateTime.now()
                )
            )
            assertEquals(2, PersonInspektør(person).antallVedtak)
        }
    }

    private fun søknad(id: String) = Søknad(
        id,
        "journalpostId-$id",
        "NAV123",
        Søknad.SøknadsType.NySøknad,
        Kanal.Digital,
        LocalDateTime.now(),
        emptyList(),
        "tittel"
    )

    private fun ettersending(søknadId: String?, ettersendingId: String?) =
        Ettersending(
            søknadId,
            ettersendingId,
            "99",
            Kanal.Digital,
            emptyList()
        )

    private class PersonInspektør(person: Person) : PersonVisitor {
        var antallVedtak = 0
        var antallEttersendinger = 0
        var antallSøknader = 0

        init {
            person.accept(this)
        }

        override fun visitSøknad(
            søknadId: String?,
            journalpostId: String,
            skjemaKode: String?,
            søknadsType: Søknad.SøknadsType,
            kanal: Kanal,
            datoInnsendt: LocalDateTime,
            tittel: String?
        ) {
            antallSøknader++
        }

        override fun visitEttersending(søknadId: String?, kanal: Kanal) {
            antallEttersendinger++
        }

        override fun visitVedtak(
            vedtakId: String,
            fagsakId: String,
            status: Vedtak.Status,
            datoFattet: LocalDateTime,
            fraDato: LocalDateTime,
            tilDato: LocalDateTime?
        ) {
            antallVedtak++
        }
    }
}
