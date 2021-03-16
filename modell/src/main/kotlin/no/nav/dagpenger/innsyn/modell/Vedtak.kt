package no.nav.dagpenger.innsyn.modell

class Vedtak private constructor(
    private val vedtakId: String,
    val søknadId: String,
    private val status: Status
) {
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
