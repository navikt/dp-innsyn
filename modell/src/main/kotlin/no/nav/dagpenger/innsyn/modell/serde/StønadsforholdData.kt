package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Stønadsforhold
import no.nav.dagpenger.innsyn.modell.Stønadsid
import no.nav.dagpenger.innsyn.modell.TilstandType
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class StønadsforholdData(
    stønadsid: Stønadsid,
    oppgaver: List<OppgaveData>,
    tilstandString: String,
) {
    internal val stønadsforhold: Stønadsforhold by lazy {
        val oppgaver = oppgaver.map { it.oppgave }.toMutableSet()

        Stønadsforhold::class.primaryConstructor!!.apply {
            isAccessible = true
        }.call(stønadsid, oppgaver, parseTilstand(TilstandType.valueOf(tilstandString)))
    }

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
