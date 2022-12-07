package no.nav.dagpenger.innsyn.tjenester

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.jackson.JacksonConverter
import mu.KotlinLogging
import no.nav.dagpenger.innsyn.tjenester.ettersending.MinimalEttersendingDto
import no.nav.dagpenger.innsyn.tjenester.ettersending.toInternal
import no.nav.dagpenger.innsyn.tjenester.paabegynt.Påbegynt
import no.nav.dagpenger.innsyn.tjenester.paabegynt.toInternal
import no.nav.dagpenger.ktor.client.metrics.PrometheusMetricsPlugin
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.seconds

private val logg = KotlinLogging.logger {}

internal class HenvendelseOppslag(
    private val dpProxyUrl: String,
    private val tokenProvider: () -> String,
    httpClientEngine: HttpClientEngine = CIO.create() {
        requestTimeout = 0
    },
    baseName: String? = null
) {

    private val dpProxyClient = HttpClient(httpClientEngine) {
        expectSuccess = true
        install(PrometheusMetricsPlugin) {
            baseName?.let { this.baseName = it }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15.seconds.inWholeMilliseconds
            connectTimeoutMillis = 5.seconds.inWholeMilliseconds
            socketTimeoutMillis = 15.seconds.inWholeMilliseconds
        }

        install(ContentNegotiation) {
            register(
                ContentType.Application.Json,
                JacksonConverter(
                    jacksonMapperBuilder()
                        .addModule(JavaTimeModule())
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                        .build()
                )
            )
        }
    }

    suspend fun hentEttersendelser(fnr: String): List<MinimalEttersendingDto> {
        return hentRequestMedFnrIBody<ExternalEttersending>(fnr, "$dpProxyUrl/proxy/v1/ettersendelser").toInternal()
    }

    suspend fun hentPåbegynte(fnr: String): List<Påbegynt> {
        return hentRequestMedFnrIBody<ExternalPåbegynt>(fnr, "$dpProxyUrl/proxy/v1/paabegynte").toInternal()
    }

    private suspend inline fun <reified T> hentRequestMedFnrIBody(fnr: String, requestUrl: String): List<T> =
        kotlin.runCatching {
            dpProxyClient.request(requestUrl) {
                method = HttpMethod.Post
                header(HttpHeaders.Authorization, "Bearer ${tokenProvider.invoke()}")
                header(HttpHeaders.ContentType, "application/json")
                header(HttpHeaders.Accept, "application/json")
                setBody(mapOf("fnr" to fnr))
            }.body<List<T>>()
        }
            .onFailure { e -> logg.error(e) { "Feil i hentRequestMedFnrIBody mot: $requestUrl" } }
            .getOrThrow()
}

data class ExternalEttersending(
    val behandlingsId: String,
    val hovedskjemaKodeverkId: String,
    val sistEndret: ZonedDateTime,
    val innsendtDato: ZonedDateTime?,
    val vedlegg: List<Vedlegg>
) {
    data class Vedlegg(val tilleggsTittel: String?, val kodeverkId: String)
}

data class ExternalPåbegynt(
    val behandlingsId: String,
    val hovedskjemaKodeverkId: String,
    val sistEndret: ZonedDateTime
)
