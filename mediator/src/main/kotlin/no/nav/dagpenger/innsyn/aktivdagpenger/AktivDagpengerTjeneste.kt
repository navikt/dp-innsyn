package no.nav.dagpenger.innsyn.aktivdagpenger

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.network.sockets.SocketTimeoutException
import no.nav.dagpenger.oauth2.CachedOauth2Client
import java.time.Clock
import java.time.LocalDate

internal interface AktivDagpengerTjeneste {
    suspend fun harAktivDagpengerett(personIdent: String): Boolean
}

internal object AlltidInaktivDagpengerTjeneste : AktivDagpengerTjeneste {
    override suspend fun harAktivDagpengerett(personIdent: String): Boolean = false
}

internal class DpDatadelingAktivDagpengerTjeneste(
    private val httpClient: HttpClient,
    private val oauth2Client: CachedOauth2Client,
    private val perioderUrl: String,
    private val scope: String,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val tokenUtsteder: (() -> String)? = null,
) : AktivDagpengerTjeneste {
    private val logger = KotlinLogging.logger {}
    private val sikkerlogg = KotlinLogging.logger("tjenestekall")

    override suspend fun harAktivDagpengerett(personIdent: String): Boolean {
        val iDag = LocalDate.now(clock)

        return try {
            val token = hentToken() ?: return false

            val request =
                PerioderRequest(
                    personIdent = personIdent,
                    fraOgMedDato = iDag,
                    tilOgMedDato = iDag,
                )

            val httpResponse: HttpResponse =
                httpClient.post(perioderUrl) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

            if (!httpResponse.status.isSuccess()) {
                sikkerlogg.warn { "Uventet HTTP-status fra dp-datadeling: ${httpResponse.status}" }
                return false
            }

            val response = httpResponse.body<PerioderResponse>()
            response.perioder.any { it.overlapper(iDag) }
        } catch (e: HttpRequestTimeoutException) {
            logger.warn { "Timeout mot dp-datadeling" }
            false
        } catch (e: SocketTimeoutException) {
            logger.warn { "Socket-timeout mot dp-datadeling" }
            false
        } catch (e: ResponseException) {
            sikkerlogg.warn(e) { "HTTP-feil fra dp-datadeling: ${e.response.status}" }
            false
        } catch (e: Exception) {
            logger.warn(e) { "Uventet feil mot dp-datadeling: ${e.message}" }
            false
        }
    }

    private fun hentToken(): String? =
        tokenUtsteder?.invoke()
            ?: oauth2Client.clientCredentials(scope).access_token
}

private data class PerioderRequest(
    @JsonProperty("personIdent")
    val personIdent: String,
    @JsonProperty("fraOgMedDato")
    val fraOgMedDato: LocalDate,
    @JsonProperty("tilOgMedDato")
    val tilOgMedDato: LocalDate?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class PerioderResponse(
    @JsonProperty("perioder")
    val perioder: List<Periode> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class Periode(
    @JsonProperty("fraOgMedDato")
    val fraOgMedDato: LocalDate,
    @JsonProperty("tilOgMedDato")
    val tilOgMedDato: LocalDate?,
) {
    fun overlapper(dato: LocalDate): Boolean = fraOgMedDato <= dato && (tilOgMedDato == null || tilOgMedDato >= dato)
}
