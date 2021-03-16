package no.nav.dagpenger.innsyn.modell

internal class Ettersending(
    val søknadId: String,
    val vedlegg: List<Vedlegg>
) {
    operator fun contains(vedlegg: Vedlegg) = this.vedlegg.contains(vedlegg)
}
