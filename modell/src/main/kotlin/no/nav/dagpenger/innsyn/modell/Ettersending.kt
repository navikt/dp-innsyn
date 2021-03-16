package no.nav.dagpenger.innsyn.modell

internal class Ettersending(
    val søknadId: String,
    private val vedlegg: List<Vedlegg>
) : Hendelse() {
    operator fun contains(vedlegg: Vedlegg) = this.vedlegg.contains(vedlegg)
}
