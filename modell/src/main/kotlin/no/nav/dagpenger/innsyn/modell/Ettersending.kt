package no.nav.dagpenger.innsyn.modell

internal class Ettersending(
    val id: String,
    private val vedlegg: List<Vedlegg>
) {
    operator fun contains(vedlegg: Vedlegg) = this.vedlegg.contains(vedlegg)
}
