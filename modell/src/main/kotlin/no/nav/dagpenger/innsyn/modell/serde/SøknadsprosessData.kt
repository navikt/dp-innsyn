package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.ProsessId
import no.nav.dagpenger.innsyn.modell.Søknadsprosess
import no.nav.dagpenger.innsyn.modell.TilstandType
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class SøknadsprosessData(
    stønadsid: ProsessId,
    oppgaver: List<OppgaveData>,
    tilstandString: String,
) {
    internal val søknadsprosess: Søknadsprosess by lazy {
        val oppgaver = oppgaver.map { it.oppgave }.toMutableSet()

        Søknadsprosess::class.primaryConstructor!!.apply {
            isAccessible = true
        }.call(stønadsid, oppgaver, parseTilstand(TilstandType.valueOf(tilstandString)))
    }

    private fun parseTilstand(tilstandType: TilstandType) =
        when (tilstandType) {
            TilstandType.START -> Søknadsprosess.Start
            TilstandType.UNDER_BEHANDLING -> Søknadsprosess.UnderBehandling
            TilstandType.LØPENDE -> Søknadsprosess.Løpende
            TilstandType.STANSET -> Søknadsprosess.Stanset
            TilstandType.AVSLÅTT -> Søknadsprosess.Avslått
            TilstandType.UTLØPT -> Søknadsprosess.Utløpt
        }
}
