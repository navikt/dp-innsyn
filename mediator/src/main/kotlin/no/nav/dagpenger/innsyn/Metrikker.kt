package no.nav.dagpenger.innsyn

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.time.Duration
import java.time.LocalDateTime

internal object Metrikker {
    private const val DAGPENGER_NAMESPACE = "dagpenger"
    private val registry: MeterRegistry = SimpleMeterRegistry()

    private fun mottakForsinkelse(label: String) =
        Timer
            .builder("${DAGPENGER_NAMESPACE}_mottak_forsinkelse")
            .tag("type", label)
            .description("Tid fra innsendingen ble journalført til vi tar i mot")
            .register(registry)

    fun søknadForsinkelse(forsinkelse: LocalDateTime) =
        mottakForsinkelse("soknad").record(
            Duration.between(forsinkelse, LocalDateTime.now()),
        )

    fun ettersendingForsinkelse(forsinkelse: LocalDateTime) =
        mottakForsinkelse("ettersending").record(
            Duration.between(forsinkelse, LocalDateTime.now()),
        )
}
