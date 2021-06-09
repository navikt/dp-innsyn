package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.hendelser.Vedtak
import java.time.LocalDateTime

interface PersonVisitor : SøknadVisitor, EttersendingVisitor, VedtakVisitor, SakstilknytningVisitor {
    fun preVisit(person: Person, fnr: String) {}
    fun postVisit(person: Person, fnr: String) {}
}

interface SøknadVisitor {
    fun visitSøknad(
        søknadId: String?,
        journalpostId: String,
        skjemaKode: String?,
        søknadsType: Søknad.SøknadsType,
        kanal: Kanal,
        datoInnsendt: LocalDateTime
    ) {}
}

interface EttersendingVisitor {
    fun visitEttersending(
        søknadId: String?,
        kanal: Kanal
    ) {}
}

interface VedtakVisitor {
    fun visitVedtak(
        vedtakId: String,
        fagsakId: String,
        status: Vedtak.Status,
        datoFattet: LocalDateTime,
        fraDato: LocalDateTime,
        tilDato: LocalDateTime?
    ) {}
}

interface SakstilknytningVisitor {
    fun visitSakstilknytning(
        journalpostId: String,
        fagsakId: String,
    ) {}
}
