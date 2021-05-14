package no.nav.dagpenger.innsyn

import io.prometheus.client.Histogram
import java.time.Duration
import java.time.LocalDateTime

internal object Metrikker {
    private const val DAGPENGER_NAMESPACE = "dagpenger"
    private val mottakForsinkelse = Histogram
        .build()
        .namespace(DAGPENGER_NAMESPACE)
        .name("mottak_forsinkelse")
        .labelNames("type")
        .help("Tid fra innsendingen ble journalført til vi tar i mot")
        .register()

    fun søknadForsinkelse(forsinkelse: LocalDateTime) =
        mottakForsinkelse.labels("soknad").observe(
            Duration.between(forsinkelse, LocalDateTime.now()).seconds.toDouble()
        )

    fun ettersendingForsinkelse(forsinkelse: LocalDateTime) =
        mottakForsinkelse.labels("ettersending").observe(
            Duration.between(forsinkelse, LocalDateTime.now()).seconds.toDouble()
        )
}
