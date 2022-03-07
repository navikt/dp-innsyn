package no.nav.dagpenger.innsyn.objectmother

import no.nav.dagpenger.innsyn.tjenester.ExternalEttersendelse
import java.time.ZonedDateTime

object ExternalEttersendelseObjectMother {
    fun giveMeDagpengeEttersendelse() =
        ExternalEttersendelse(
            "bid",
            "NAV 04-01.03",
            ZonedDateTime.now(),
            ZonedDateTime.now(),
            emptyList()
        )

    fun giveMeBILEttersendelse() = ExternalEttersendelse(
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
