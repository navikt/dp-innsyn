package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.Plan
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class PersonData(
    fnr: String,
    kjeder: List<Pair<String, String>>,
    oppgaver: List<Pair<String, Oppgave>>
) {
    private val behandlingskjeder = kjeder.map { pair ->
        Plan::class.primaryConstructor!!.apply {
            isAccessible = true
        }.call(pair.first, oppgaver.filter { it.first == pair.second }.map { it.second }.toSet())
    }
    val person = Person(fnr).also {
        it.javaClass.getDeclaredField("behandlingskjeder").apply {
            isAccessible = true
        }.set(it, behandlingskjeder.toSet())
    }
}
