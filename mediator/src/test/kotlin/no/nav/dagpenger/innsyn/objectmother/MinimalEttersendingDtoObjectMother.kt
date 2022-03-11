package no.nav.dagpenger.innsyn.objectmother

import no.nav.dagpenger.innsyn.tjenester.ettersending.MinimalEttersendingDto
import java.time.ZonedDateTime

object MinimalEttersendingDtoObjectMother {

    private val eldsteLovligeInnsendingsdatoAngittIDager: Long = (365 * 3) - 1

    fun giveMeEttersending(
        søknadId: String = "sId-123",
        innsendtDato: ZonedDateTime? = ZonedDateTime.now().minusDays(eldsteLovligeInnsendingsdatoAngittIDager),
        tittel: String = "Dette er en ettersending"
    ) = MinimalEttersendingDto(
        søknadId,
        innsendtDato,
        tittel
    )

    fun giveMeForGammelEttersending() = giveMeEttersending(innsendtDato = ZonedDateTime.now().minusYears(4))

    fun giveMeEttersendingUtenInnsendtDato(søknadId: String = "123") = giveMeEttersending(innsendtDato = null, søknadId = søknadId)
}
