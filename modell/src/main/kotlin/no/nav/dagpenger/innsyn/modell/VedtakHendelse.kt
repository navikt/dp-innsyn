package no.nav.dagpenger.innsyn.modell

internal class VedtakHendelse(val meldingsreferanseId: String, val id: String, utfall: String) : Hendelse(meldingsreferanseId) {

    fun vedtak(): Vedtak = Vedtak(id, "", Vedtak.Status.INNVILGET)
}
