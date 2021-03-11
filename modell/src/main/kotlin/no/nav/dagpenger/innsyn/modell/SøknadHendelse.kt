package no.nav.dagpenger.innsyn.modell

internal class SøknadHendelse(val meldingsreferanseId: String, val id: String) : Hendelse(meldingsreferanseId) {

    fun søknad(): Søknad = Søknad(id)
}
