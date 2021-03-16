package no.nav.dagpenger.innsyn.modell

internal class Vedtak private constructor(
    private val vedtakId: String,
    private val søknadId: String,
    private val status: Status
) : Hendelse() {
    constructor(
        vedtakId: String
    ) : this(
        vedtakId,
        "",
        Status.INNVILGET
    )

    constructor(
        vedtakId: String,
        søknadId: String
    ) : this(
        vedtakId,
        søknadId,
        Status.INNVILGET
    )

    enum class Status {
        INNVILGET,
        AVSLÅTT
    }
}
