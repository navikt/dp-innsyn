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
    stønadsforhold: List<StønadsforholdData>,
) {
    private val stønadsforhold = stønadsforhold.map { it.stønadsforhold }
     val person = Person(fnr).also {
         it.javaClass.getDeclaredField("stønadsforhold").apply {
             isAccessible = true
         }.set(it, this.stønadsforhold)
     }
}
