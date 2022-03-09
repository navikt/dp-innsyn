package no.nav.dagpenger.innsyn.objectmother

import no.nav.dagpenger.innsyn.common.KildeType
import no.nav.dagpenger.innsyn.common.MultiSourceResult
import no.nav.dagpenger.innsyn.tjenester.ettersending.MinimalEttersendingDto

object MultiSourceResultObjectMother {

    fun giveMeSuccessfulResult(source: KildeType = KildeType.HENVENDELSE) =
        MultiSourceResult.createSuccessfulResult(
            listOf(
                MinimalEttersendingDtoObjectMother.giveMeEttersending()
            ),
            source
        )

    fun giveMeFailedResult(source: KildeType) =
        MultiSourceResult.createErrorResult<MinimalEttersendingDto, KildeType>(source)
}
