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
            person.håndter(Vedtak("1", "2", Vedtak.Status.INNVILGET))
            person.håndter(Vedtak("5", "6", Vedtak.Status.AVSLÅTT))
            assertEquals(2, PersonInspektør(person).antallVedtak)
        }
    }

    private fun søknad(id: String) = Søknad(
        id,
        "journalpostId",
        "NAV123",
        Søknad.SøknadsType.NySøknad,
        Kanal.Digital,
        LocalDateTime.now()
    )
    private fun ettersending(søknadId: String?, ettersendingId: String?) = Ettersending(søknadId, ettersendingId, "99", Kanal.Digital)

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
            datoInnsendt: LocalDateTime
        ) {
            antallSøknader++
        }

        override fun visitEttersending(søknadId: String?, kanal: Kanal) {
            antallEttersendinger++
        }

        override fun visitVedtak(vedtakId: String, fagsakId: String, status: Vedtak.Status) {
            antallVedtak++
        }
    }
}
