package no.nav.dagpenger.innsyn.modell

internal class VedtakHendelse(
    meldingsreferanseId: String,
    val id: String,
    utfall: String
) : Hendelse(meldingsreferanseId) {
    private val utfall = Vedtak.Status.valueOf(utfall.toUpperCase())

    fun vedtak(): Vedtak = Vedtak(id, "", this.utfall)
}
