package no.nav.dagpenger.innsyn.tjenester.paabegynt

import no.nav.dagpenger.innsyn.tjenester.ExternalPåbegynt
import no.nav.dagpenger.innsyn.tjenester.ettersending.dagpengeBrevkoder

internal fun List<ExternalPåbegynt>.toInternal(): List<Påbegynt> {
    val dagpengerPåbegynte = filter { externalPåbegynt ->
        dagpengeBrevkoder.containsKey(externalPåbegynt.hovedskjemaKodeverkId)
    }.map { externalPåbegynt ->
        externalPåbegynt.toInternal()
    }
    return dagpengerPåbegynte
}

internal fun ExternalPåbegynt.toInternal() = Påbegynt(
    tittel = dagpengeBrevkoder[this.hovedskjemaKodeverkId]
        ?: throw IllegalArgumentException("$hovedskjemaKodeverkId er ikke en dagpengekode."),
    behandlingsId = this.behandlingsId,
    sistEndret = this.sistEndret,
)
