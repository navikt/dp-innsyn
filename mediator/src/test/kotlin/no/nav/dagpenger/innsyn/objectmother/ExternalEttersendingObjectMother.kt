package no.nav.dagpenger.innsyn.objectmother

import no.nav.dagpenger.innsyn.tjenester.ExternalEttersending
import java.time.ZonedDateTime

object ExternalEttersendingObjectMother {
    fun giveMeDagpengeEttersendelse() =
        ExternalEttersending(
            "bid",
            "NAV 04-01.03",
            ZonedDateTime.now(),
            ZonedDateTime.now(),
            emptyList()
        )

    fun giveMeBILEttersendelse() = ExternalEttersending(
        "bid",
        "NAV 10-07.40",
        ZonedDateTime.now(),
        ZonedDateTime.now(),
        emptyList()
    )

    fun giveMeEttersendelserForDAGOgBIL() = listOf(
        giveMeBILEttersendelse(),
        giveMeDagpengeEttersendelse()
    )
}
