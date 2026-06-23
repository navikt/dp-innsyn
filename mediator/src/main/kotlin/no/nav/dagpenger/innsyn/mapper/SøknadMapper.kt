package no.nav.dagpenger.innsyn.mapper

import no.nav.dagpenger.innsyn.api.models.SoknadResponse
import no.nav.dagpenger.innsyn.api.models.VedleggResponse
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.tjenester.Lenker
import java.util.UUID

object SøknadMapper {
    fun Søknad.toResponse(): SoknadResponse =
        SoknadResponse(
            journalpostId = journalpostId,
            søknadsType = SoknadResponse.SøknadsType.valueOf(søknadsType.toString()),
            kanal = SoknadResponse.Kanal.valueOf(kanal.toString()),
            datoInnsendt = datoInnsendt,
            søknadId = søknadId,
            erNySøknadsdialog = søknadId?.erFraNySøknadsdialog(),
            endreLenke =
                søknadId?.let { id ->
                    if (id.erFraNySøknadsdialog()) {
                        Lenker.ettersendelseNySøknadsdialog(id)
                    } else {
                        Lenker.ettersendelseGammelSøknadsdialog(id)
                    }
                },
            skjemaKode = skjemaKode,
            tittel = tittel,
            vedlegg =
                vedlegg.map { v ->
                    VedleggResponse(
                        skjemaNummer = v.skjemaNummer,
                        navn = v.navn,
                        status = VedleggResponse.Status.valueOf(v.status.toString()),
                    )
                },
        )

    private fun String.erFraNySøknadsdialog(): Boolean = runCatching { UUID.fromString(this) }.isSuccess
}
