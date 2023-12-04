package no.nav.dagpenger.innsyn.objectmother

import no.nav.dagpenger.innsyn.tjenester.ExternalPåbegynt
import java.time.ZonedDateTime

object ExternalPåbegyntObjectMother {
    fun giveMeDagpengePåbegynt() =
        ExternalPåbegynt(
            "bid",
            "NAV 04-01.03",
            ZonedDateTime.now(),
        )

    fun giveMeBILPåbegynt() =
        ExternalPåbegynt(
            "bid",
            "NAV 10-07.40",
            ZonedDateTime.now(),
        )

    fun giveMePåbegynteForDAGOgBIL() =
        listOf(
            giveMeBILPåbegynt(),
            giveMeDagpengePåbegynt(),
        )
}
