package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Stønadsforhold
import no.nav.dagpenger.innsyn.modell.Stønadsid
import no.nav.dagpenger.innsyn.modell.TilstandType
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class StønadsforholdData(
    private val internId: String,
    private val oppgaver: List<OppgaveData>,
    private val tilstandString: String,
) {
    private val etfintnavn = oppgaver.map { it.oppgave }.toMutableSet()
    private val stønadsid = Stønadsid(internId, mutableListOf())
    internal val stønadsforhold: Stønadsforhold
        get() =
            Stønadsforhold::class.primaryConstructor!!.apply {
                isAccessible = true
            }.call(stønadsid, etfintnavn, parseTilstand(TilstandType.valueOf(tilstandString)))

    private fun parseTilstand(tilstandType: TilstandType) =
        when (tilstandType) {
            TilstandType.START -> Stønadsforhold.Start
            TilstandType.UNDER_BEHANDLING -> Stønadsforhold.UnderBehandling
            TilstandType.LØPENDE -> Stønadsforhold.Løpende
            TilstandType.STANSET -> Stønadsforhold.Stanset
            TilstandType.AVSLÅTT -> Stønadsforhold.Avslått
            TilstandType.UTLØPT -> Stønadsforhold.Utløpt
        }
}