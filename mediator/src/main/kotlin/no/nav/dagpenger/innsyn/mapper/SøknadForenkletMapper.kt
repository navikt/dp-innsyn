package no.nav.dagpenger.innsyn.mapper

import no.nav.dagpenger.innsyn.api.models.SoknadResponse
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.serde.SøknadVisitor
import java.time.LocalDateTime

class SøknadForenkletMapper(
    val søknad: Søknad,
) : SøknadVisitor {
    private var søknadId: String? = null
    private var skjemakode: String? = null

    private lateinit var journalpostId: String
    private lateinit var søknadsType: SoknadResponse.SøknadsType
    private lateinit var kanal: SoknadResponse.Kanal
    private lateinit var datoInnsendt: LocalDateTime

    init {
        søknad.accept(this)
    }

    val response: SoknadResponse get() =
        SoknadResponse(
            journalpostId = journalpostId,
            søknadsType = søknadsType,
            kanal = kanal,
            datoInnsendt = datoInnsendt,
            søknadId = søknadId,
            skjemaKode = skjemakode,
        )

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
            this.søknadId = søknadIden
        }

        this.journalpostId = journalpostId
        this.skjemakode = skjemaKode
        this.søknadsType = SoknadResponse.SøknadsType.valueOf(søknadsType.toString())
        this.kanal = SoknadResponse.Kanal.valueOf(kanal.toString())
        this.datoInnsendt = datoInnsendt
    }

    override fun visitVedlegg(
        skjemaNummer: String,
        navn: String,
        status: Innsending.Vedlegg.Status,
    ) {
    }
}
