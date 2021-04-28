package no.nav.dagpenger.innsyn.modell.hendelser

class Vedtak private constructor(
    private val vedtakId: String,
    val fagsakId: String,
    public val status: Status,
    oppgaver: Set<Oppgave>
) : Hendelse(oppgaver) {
    constructor(
        vedtakId: String,
        fagsakId: String,
        status: Status
    ) : this(
        vedtakId,
        fagsakId,
        status,
        emptySet()
    )

    constructor(
        vedtakId: String,
        fagsakId: String,
        oppgaver: Set<Oppgave>,
        status: Status
    ) : this(
        vedtakId,
        fagsakId,
        status,
        oppgaver
    )

    enum class Status {
        INNVILGET,
        AVSLÅTT,
        STANS,
        ENDRING
    }
}
