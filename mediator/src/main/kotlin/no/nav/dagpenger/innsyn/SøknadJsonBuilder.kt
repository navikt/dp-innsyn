package no.nav.dagpenger.innsyn

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.serde.SøknadVisitor
import no.nav.dagpenger.innsyn.tjenester.Lenker
import java.time.LocalDateTime
import java.util.UUID

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
        tittel: String?,
    ) {
        søknadId?.let { søknadIden ->
            root.put("søknadId", søknadIden)
            root.put("erNySøknadsdialog", søknadIden.erFraNySøknadsdialog())
            root.put("endreLenke", if (søknadIden.erFraNySøknadsdialog()) Lenker.ettersendelseNySøknadsdialog(søknadIden) else Lenker.ettersendelseGammelSøknadsdialog(søknadIden))
        }
        skjemaKode?.let {
            root.put("skjemaKode", it)
            finnTittel(it)?.let { tittel -> root.put("tittel", tittel) }
        }
        tittel?.let { root.put("tittel", it) }
        root.put("journalpostId", journalpostId)
        root.put("søknadsType", søknadsType.toString())
        root.put("kanal", kanal.toString())
        root.put("datoInnsendt", datoInnsendt.toString())
        root.set<ArrayNode>("vedlegg", vedlegg)
    }

    // TODO: Dette kan fjernes så fort vi er fullstendig over på ny søknadsdialog
    private fun String.erFraNySøknadsdialog(): Boolean = this.runCatching {
        UUID.fromString(this)
    }.isSuccess

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
