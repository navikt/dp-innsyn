package no.nav.dagpenger.innsyn.modell

internal class EttersendingHendelse(val meldingsreferanseId: String, val id: String, val vedlegg: Vedlegg) : Hendelse(meldingsreferanseId) {
    fun ettersending(): Ettersending = Ettersending(id, emptyList())
}
