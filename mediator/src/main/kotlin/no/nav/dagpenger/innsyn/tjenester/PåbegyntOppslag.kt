package no.nav.dagpenger.innsyn.tjenester

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import java.time.ZonedDateTime
import java.util.UUID

internal class PåbegyntOppslag(
    private val baseUrl: String,
    private val soknadAudience: String,
    private val tokenProvider: (token: String, audience: String) -> String = exchangeToOboToken,
    engine: HttpClientEngine = CIO.create()
) {

    private val httpClient = HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) {
            jackson {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                registerModule(JavaTimeModule())
            }
        }
    }

    internal suspend fun hentPåbegyntSøknad(subjectToken: String): PåbegyntSøknadDto {
        val url = "$baseUrl/soknad/paabegynt"
        return httpClient.get(url) {
            addBearerToken(subjectToken)
            contentType(ContentType.Application.Json)
        }.body()
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
    val sistEndret: ZonedDateTime
)

private val exchangeToOboToken = { token: String, audience: String ->
    no.nav.dagpenger.innsyn.Configuration.tokenXClient.tokenExchange(token, audience).accessToken
}
