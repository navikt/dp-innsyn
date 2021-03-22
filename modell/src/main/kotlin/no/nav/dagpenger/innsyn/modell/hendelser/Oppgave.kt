package no.nav.dagpenger.innsyn.modell.hendelser

import no.nav.dagpenger.innsyn.modell.Vedlegg

abstract class Oppgave(id: String) {
    var status: String = "Uferdig"

}

class VedleggOppgave(id: String, navn: String): Oppgave(id){
}


class VedtakOppgave(id: String): Oppgave(id){
}

