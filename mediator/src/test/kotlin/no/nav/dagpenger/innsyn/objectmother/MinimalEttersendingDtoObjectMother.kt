package no.nav.dagpenger.innsyn.objectmother

import no.nav.dagpenger.innsyn.tjenester.ettersending.MinimalEttersendingDto
import java.time.ZonedDateTime

object MinimalEttersendingDtoObjectMother {

    fun giveMeEttersending(
        søknadId: String = "sId-123",
        innsendtDato: ZonedDateTime? = ZonedDateTime.now(),
        tittel: String = "Dette er en ettersending"
    ) = MinimalEttersendingDto(
        søknadId,
        innsendtDato,
        tittel
    )
}
