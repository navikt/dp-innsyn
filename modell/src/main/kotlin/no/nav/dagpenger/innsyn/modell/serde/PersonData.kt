package no.nav.dagpenger.innsyn.modell.serde

import no.nav.dagpenger.innsyn.modell.Person
import no.nav.dagpenger.innsyn.modell.hendelser.Oppgave
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class PersonData(
    fnr: String,
    oppgaver: List<Oppgave>,
) {
    val person = Person::class.primaryConstructor!!
        .apply { isAccessible = true }
        .call(fnr, oppgaver)
}
