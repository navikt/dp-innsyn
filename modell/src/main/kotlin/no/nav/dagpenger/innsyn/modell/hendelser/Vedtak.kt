package no.nav.dagpenger.innsyn.modell.hendelser

class Vedtak private constructor(
    private val vedtakId: String,
    val søknadId: String,
    public val status: Status,
    oppgaver: Set<Oppgave>
) : Hendelse(søknadId, oppgaver) {
    constructor(
        vedtakId: String
    ) : this(
        vedtakId,
        "",
        Status.INNVILGET,
        emptySet()
    )

    constructor(
        vedtakId: String,
        søknadId: String,
        oppgaver: Set<Oppgave>
    ) : this(
        vedtakId,
        søknadId,
        Status.INNVILGET,
        oppgaver
    )

    enum class Status {
        INNVILGET,
        AVSLÅTT
    }
}
