package no.nav.dagpenger.innsyn.mapper

import no.nav.dagpenger.innsyn.api.models.SoknadResponse
import no.nav.dagpenger.innsyn.api.models.VedleggResponse
import no.nav.dagpenger.innsyn.modell.hendelser.Innsending
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.serde.SøknadVisitor
import no.nav.dagpenger.innsyn.tjenester.Lenker
import java.time.LocalDateTime
import java.util.UUID

class SøknadMapper(
    val søknad: Søknad,
) : SøknadVisitor {
    private var søknadId: String? = null
    private var tittel: String? = null
    private var erNySøknadsdialog: Boolean? = null
    private var skjemakode: String? = null
    private var endreLenke: String? = null
    private var vedlegg: MutableList<VedleggResponse> = mutableListOf()

    private lateinit var journalpostId: String
    private lateinit var søknadsType: SoknadResponse.SøknadsType
    private lateinit var kanal: SoknadResponse.Kanal
    private lateinit var datoInnsendt: LocalDateTime

    init {
        søknad.accept(this)
    }

    private fun String.erFraNySøknadsdialog(): Boolean =
        this
            .runCatching {
                UUID.fromString(this)
            }.isSuccess

    val response: SoknadResponse get() =
        SoknadResponse(
            journalpostId = journalpostId,
            søknadsType = søknadsType,
            kanal = kanal,
            datoInnsendt = datoInnsendt,
            søknadId = søknadId,
            erNySøknadsdialog = erNySøknadsdialog,
            endreLenke = endreLenke,
            skjemaKode = skjemakode,
            tittel = tittel,
            vedlegg = vedlegg,
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
            erNySøknadsdialog = søknadIden.erFraNySøknadsdialog()
            endreLenke =
                if (søknadIden.erFraNySøknadsdialog()) {
                    Lenker.ettersendelseNySøknadsdialog(
                        søknadIden,
                    )
                } else {
                    Lenker.ettersendelseGammelSøknadsdialog(søknadIden)
                }
        }

        this.tittel = tittel
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
        vedlegg.add(
            VedleggResponse(
                skjemaNummer = skjemaNummer,
                navn = navn,
                status = VedleggResponse.Status.valueOf(status.toString()),
            ),
        )
    }
}
