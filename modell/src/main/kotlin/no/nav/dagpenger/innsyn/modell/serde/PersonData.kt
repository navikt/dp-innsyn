package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Behandlingskjede
import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class PersonData(
    fnr: String,
    kjeder: Map<String, List<String>>,
    oppgaver: Map<String, Oppgave>
) {
    private val behandlingskjeder = kjeder.map { kjede ->
        Behandlingskjede::class.primaryConstructor!!.apply {
            isAccessible = true
        }.call(kjede.key, oppgaver.filter { kjede.value.contains(it.key) }.map { it.value }.toSet())
    }
    val person = Person(fnr).also {
        it.javaClass.getDeclaredField("behandlingskjeder").apply {
            isAccessible = true
        }.set(it, behandlingskjeder.toSet())
    }
}
