package no.nav.dagpenger.innsyn.modell.serde

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import java.time.LocalDateTime

class SøknadJsonBuilder(val søknad: Søknad) : SøknadVisitor {
    private val mapper = ObjectMapper()
    private val root = mapper.createObjectNode()

    init {
        søknad.accept(this)
    }

    fun resultat() = root

    override fun visitSøknad(
        søknadId: String?,
        journalpostId: String,
        skjemaKode: String?,
        søknadsType: Søknad.SøknadsType,
        kanal: Kanal,
        datoInnsendt: LocalDateTime
    ) {
        søknadId?.let { root.put("søknadId", søknadId) }
        skjemaKode?.let { root.put("skjemaKode", skjemaKode) }
        root.put("journalpostId", journalpostId)
        root.put("søknadsType", søknadsType.toString())
        root.put("kanal", kanal.toString())
        root.put("datoInnsendt", datoInnsendt.toString())
    }
}
