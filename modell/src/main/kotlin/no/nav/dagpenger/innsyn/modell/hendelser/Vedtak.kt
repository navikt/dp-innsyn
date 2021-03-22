package no.nav.dagpenger.innsyn.modell.hendelser

class Vedtak private constructor(
    private val vedtakId: String,
    val søknadId: String,
    private val status: Status,
    oppgaver: List<Oppgave>
) : Hendelse(oppgaver) {
    constructor(
        vedtakId: String
    ) : this(
        vedtakId,
        "",
        Status.INNVILGET,
        emptyList()
    )

    constructor(
        vedtakId: String,
        søknadId: String,
        oppgaver: List<Oppgave>
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
