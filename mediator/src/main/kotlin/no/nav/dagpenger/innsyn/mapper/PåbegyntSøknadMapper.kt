package no.nav.dagpenger.innsyn.mapper

import no.nav.dagpenger.innsyn.api.models.PaabegyntSoknadResponse
import no.nav.dagpenger.innsyn.tjenester.Lenker
import no.nav.dagpenger.innsyn.tjenester.PåbegyntSøknadDto

class PåbegyntSøknadMapper(val dto: PåbegyntSøknadDto, val erNySøknadsdialog: Boolean) {
    val response: PaabegyntSoknadResponse get() =
        PaabegyntSoknadResponse(
            søknadId = dto.uuid.toString(),
            behandlingsId = dto.uuid.toString(),
            tittel = "Søknad om dagpenger",
            sistEndret = dto.sistEndret.toLocalDateTime(),
            erNySøknadsdialog = erNySøknadsdialog,
            endreLenke =
                if (erNySøknadsdialog) {
                    Lenker.påbegyntNySøknadsdialogIngress(
                        dto.uuid.toString(),
                    )
                } else {
                    Lenker.påbegyntGammelSøknadsdialogIngress(dto.uuid.toString())
                },
        )
}
