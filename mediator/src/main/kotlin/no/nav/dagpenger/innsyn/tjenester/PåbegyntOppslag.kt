package no.nav.dagpenger.innsyn.tjenester

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

internal class PåbegyntOppslag(
    private val baseUrl: String,
    private val soknadAudience: String,
    private val tokenProvider: (token: String, audience: String) -> String? = exchangeToOboToken,
    engine: HttpClientEngine =
        CIO.create {
            requestTimeout = 0
        },
) {
    private val httpClient =
        HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) {
                jackson {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                    registerModule(JavaTimeModule())
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15.seconds.inWholeMilliseconds
                connectTimeoutMillis = 5.seconds.inWholeMilliseconds
                socketTimeoutMillis = 15.seconds.inWholeMilliseconds
            }
        }

    internal suspend fun hentPåbegyntSøknad(
        subjectToken: String,
        XRequestId: String? = UUID.randomUUID().toString(),
    ): PåbegyntSøknadDto? {
        val url = "$baseUrl/arbeid/dagpenger/soknadapi/soknad/paabegynt"
        return try {
            httpClient
                .get(url) {
                    addBearerToken(subjectToken)
                    header(HttpHeaders.XRequestId, XRequestId)
                    contentType(ContentType.Application.Json)
                }.body()
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.NotFound -> null
                else -> throw e
            }
        }
    }

    private fun HttpRequestBuilder.addBearerToken(subjectToken: String) {
        headers[HttpHeaders.Authorization] = "Bearer ${tokenProvider.invoke(subjectToken, soknadAudience)}"
    }
}

data class PåbegyntSøknadDto(
    @JsonProperty("uuid")
    val uuid: UUID,
    @JsonProperty("opprettet")
    val opprettet: ZonedDateTime,
    @JsonProperty("sistEndret")
    val sistEndret: ZonedDateTime,
)

private val exchangeToOboToken = { token: String, audience: String ->
    no.nav.dagpenger.innsyn.Configuration.tokenXClient
        .tokenExchange(token, audience)
        .access_token
}
