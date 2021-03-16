package no.nav.dagpenger.innsyn.modell

internal class Søknad private constructor(
    private val id: String,
    private val vedlegg: MutableList<Vedlegg>,
    private val oppgaver: MutableList<Oppgave>,
    private val vedtak: MutableList<Vedtak>
) {
    constructor(id: String) : this(id, mutableListOf(), mutableListOf(), mutableListOf())
    constructor(id: String, vedlegg: List<Vedlegg>, oppgave: List<Oppgave>) : this(
        id,
        vedlegg.toMutableList(),
        oppgave.toMutableList(),
        mutableListOf()
    )

    internal fun håndter(ettersending: Ettersending) {
        this.vedlegg.addAll(ettersending.vedlegg)
    }

    internal fun håndter(vedtak: Vedtak) {
        if (vedtak.søknadId != id) return
        this.vedtak.add(vedtak)
    }

    fun harVedtak() =
        vedtak.isNotEmpty()
}
