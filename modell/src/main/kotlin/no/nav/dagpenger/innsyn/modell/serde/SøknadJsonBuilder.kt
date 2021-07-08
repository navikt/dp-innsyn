package no.nav.dagpenger.innsyn.modell.serde

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import java.time.LocalDateTime

class SøknadJsonBuilder(val søknad: Søknad) : SøknadVisitor {
    private val mapper = ObjectMapper()
    private val root = mapper.createObjectNode()
    private val vedlegg = mapper.createArrayNode()

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
        datoInnsendt: LocalDateTime,
        tittel: String?
    ) {
        søknadId?.let { root.put("søknadId", it) }
        skjemaKode?.let {
            root.put("skjemaKode", it)
            finnTittel(it)?.let { tittel -> root.put("tittel", tittel) }
        }
        tittel?.let { root.put("tittel", it) }
        root.put("journalpostId", journalpostId)
        root.put("søknadsType", søknadsType.toString())
        root.put("kanal", kanal.toString())
        root.put("datoInnsendt", datoInnsendt.toString())
        root.put("vedlegg", vedlegg)
    }

    override fun visitVedlegg(skjemaNummer: String, navn: String, status: Innsending.Vedlegg.Status) {
        vedlegg.addObject().also {
            it.put("skjemaNummer", skjemaNummer)
            it.put("navn", navn)
            it.put("status", status.toString())
        }
    }

    private fun finnTittel(skjemaNummer: String) = mapOf(
        "NAV 04-16.04" to "Søknad om gjenopptak av dagpenger ved permittering",
        "NAV 04-16.03" to "Søknad om gjenopptak av dagpenger",
        "NAV 04-01.03" to "Søknad om dagpenger (ikke permittert)",
        "NAV 04-01.04" to "Søknad om dagpenger ved permittering",
    )[skjemaNummer]
}
