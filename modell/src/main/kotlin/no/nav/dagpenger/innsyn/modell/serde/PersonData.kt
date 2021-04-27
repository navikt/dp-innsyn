package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.Stønadsforhold
import no.nav.dagpenger.innsyn.modell.Stønadsid
import no.nav.dagpenger.innsyn.modell.TilstandType
import java.time.LocalDateTime
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class PersonData(
    fnr: String,
    stønadsforhold: List<String>,
    oppgaver: List<OppgaveData>
) {
    private val behandlingskjeder = stønadsforhold.map { id ->
        val etfintnavn = oppgaver.filter { it.stønadsid == id}.map { it.oppgave }.toMutableSet()
        val stønadsid = Stønadsid(id, mutableListOf())
         Stønadsforhold::class.primaryConstructor!!.apply {
             isAccessible = true
         }.call(oppgaver, parseTilstand(TilstandType.START), LocalDateTime.now(), LocalDateTime.now(), stønadsid)
     }

    private fun parseTilstand(tilstandType: TilstandType) =
        when(tilstandType) {
            TilstandType.START -> Stønadsforhold.Start
            TilstandType.UNDER_BEHANDLING -> Stønadsforhold.UnderBehandling
            TilstandType.LØPENDE -> Stønadsforhold.Løpende
            TilstandType.STANSET -> Stønadsforhold.Stanset
            TilstandType.AVSLÅTT -> Stønadsforhold.Avslått
            TilstandType.UTLØPT -> Stønadsforhold.Utløpt
        }

    private val stønadsforhold = mutableSetOf<Stønadsforhold>()
     val person = Person(fnr).also {
         it.javaClass.getDeclaredField("stønadsforhold").apply {
             isAccessible = true
         }.set(it, this.behandlingskjeder)
     }
}
