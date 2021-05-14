package no.nav.dagpenger.innsyn.modell.hendelser

class Vedtak constructor(
    private val vedtakId: String,
    val fagsakId: String,
    oppgaver: Set<Oppgave>,
    val status: Status,
) : Hendelse(oppgaver) {
    constructor(
        vedtakId: String,
        fagsakId: String,
        status: Status
    ) : this(
        vedtakId,
        fagsakId,
        emptySet(),
        status,
    )

    enum class Status {
        INNVILGET,
        AVSLÅTT,
        STANS,
        ENDRING
    }
}
