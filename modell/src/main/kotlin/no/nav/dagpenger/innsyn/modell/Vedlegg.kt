package no.nav.dagpenger.innsyn.modell

internal class Vedlegg(private val id: String) {

    abstract class Tilstand {
        class IkkeInnsendt : Tilstand()
        class Innsendt : Tilstand()
    }
    var tilstand: Tilstand = Tilstand.IkkeInnsendt()

    fun håndter(ettersending: Ettersending){
        if(ettersending.id == id){
            tilstand = Tilstand.Innsendt()
        }
    }

}
