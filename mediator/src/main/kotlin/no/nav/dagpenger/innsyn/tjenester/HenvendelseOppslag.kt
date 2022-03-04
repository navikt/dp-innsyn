package no.nav.dagpenger.innsyn.tjenester

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import mu.KotlinLogging
import java.time.ZonedDateTime

private val logger = KotlinLogging.logger {}

internal class HenvendelseOppslag(
    private val dpProxyUrl: String,
    private val tokenProvider: () -> String,
    httpClientEngine: HttpClientEngine = CIO.create()
) {

    private val dpProxyClient = HttpClient(httpClientEngine) {

        install(DefaultRequest) {
        }
        install(JsonFeature) {
            serializer = JacksonSerializer(
                jackson = jacksonMapperBuilder()
                    .addModule(JavaTimeModule())
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .build()
            )
        }
    }

    suspend fun hentEttersendelser(fnr: String): Ettersendelse {
        return dpProxyClient.request("$dpProxyUrl/proxy/v1/ettersendelser") {
            method = HttpMethod.Post
            header(HttpHeaders.Authorization, "Bearer ${tokenProvider.invoke()}")
            header(HttpHeaders.ContentType, "application/json")
            header(HttpHeaders.Accept, "application/json")
            body = mapOf("fnr" to fnr)
        }
    }
}

data class Ettersendelse(
    val behandlingsId: String,
    val hovedskjemaKodeverkId: String,
    val sistEndret: ZonedDateTime,
    val innsendtDato: ZonedDateTime?,
    val vedlegg: List<Vedlegg>
) {
    data class Vedlegg(val tilleggsTittel: String?, val kodeverkId: String)
}
