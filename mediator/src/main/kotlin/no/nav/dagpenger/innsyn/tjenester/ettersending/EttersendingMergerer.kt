package no.nav.dagpenger.innsyn.tjenester.ettersending

import no.nav.dagpenger.innsyn.db.PersonRepository
import no.nav.dagpenger.innsyn.modell.hendelser.Kanal
import no.nav.dagpenger.innsyn.modell.hendelser.Søknad
import no.nav.dagpenger.innsyn.modell.serde.SøknadVisitor
import no.nav.dagpenger.innsyn.tjenester.HenvendelseOppslag
import java.time.LocalDateTime
import java.time.ZonedDateTime

internal class EttersendingMergerer(
    val henvendelseOppslag: HenvendelseOppslag,
    val personRepository: PersonRepository
) {

    suspend fun hentEttersendinger(fnr: String): List<MinimalEttersendingDto> {
        val ettersendingerFraHenvendelse = henvendelseOppslag.hentEttersendelser(fnr)
        val søknaderFraDb = personRepository.hentSøknaderFor(fnr)

        søknaderFraDb.toMinimalEttersending()
        return emptyList()
    }
}

internal fun List<Søknad>.toMinimalEttersending(): List<MinimalEttersendingDto> {
    return map { søknad ->
        søknad.toMinimalEttersendingDto()
    }
}

private fun Søknad.toMinimalEttersendingDto(): MinimalEttersendingDto {
    return MinimalEttersendingDto("", ZonedDateTime.now(), "")
}

class MinimalEttersendelseDtoBuilder(val søknad: Søknad) : SøknadVisitor {

    lateinit var resultat: MinimalEttersendingDto

    init {
        søknad.accept(this)
    }

    override fun visitSøknad(
        søknadId: String?,
        journalpostId: String,
        skjemaKode: String?,
        søknadsType: Søknad.SøknadsType,
        kanal: Kanal,
        datoInnsendt: LocalDateTime,
        tittel: String?
    ) {
        // resultat = MinimalEttersendingDto(søknadId, datoInnsendt, tittel)
    }
}
