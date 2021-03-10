package no.nav.dagpenger.innsyn.modell

internal class Søknad(id: String) {
    val tilstand: Tilstand = Tilstand.Innsendt()

    abstract class Tilstand {
        class Innsendt : Tilstand()
    }
}
