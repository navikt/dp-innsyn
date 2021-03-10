package no.nav.dagpenger.innsyn.modell

internal class Vedtak(vedtakId: String, val søknadId: String, status: Status) {

    enum class Status {
        INNVILGET,
        AVSLÅTT
    }
}
