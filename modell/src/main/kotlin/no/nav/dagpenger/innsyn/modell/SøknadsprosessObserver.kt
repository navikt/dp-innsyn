package no.nav.dagpenger.innsyn.modell

import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

interface SøknadsprosessObserver {
    data class EndretTilstandEvent(
        val gjeldendeTilstand: TilstandType,
        val forrigeTilstand: TilstandType,
    )

    data class FerdigBehandletEvent(
        val internId: UUID,
        val opprettet: LocalDateTime,
        val ferdig: LocalDateTime,
        val tidBrukt: Duration
    )

    fun tilstandEndret(event: EndretTilstandEvent) {}
    fun ferdigBehandlet(event: FerdigBehandletEvent) {}
}
