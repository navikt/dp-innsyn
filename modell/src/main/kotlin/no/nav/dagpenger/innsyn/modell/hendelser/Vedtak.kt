package no.nav.dagpenger.innsyn.modell.hendelser

class Vedtak private constructor(
    private val vedtakId: String,
    val søknadId: String,
    public val status: Status,
    oppgaver: Set<Oppgave>
) : Hendelse(søknadId, oppgaver) {
    constructor(
        vedtakId: String,
        status: Status
    ) : this(
        vedtakId,
        "",
        status,
        emptySet()
    )

    constructor(
        vedtakId: String,
        søknadId: String,
        oppgaver: Set<Oppgave>,
        status: Status
    ) : this(
        vedtakId,
        søknadId,
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
