package no.nav.dagpenger.innsyn

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.natpryce.konfig.PropertyGroup
import com.natpryce.konfig.getValue
import com.natpryce.konfig.stringType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.jackson.jackson
import io.ktor.server.auth.jwt.JWTAuthenticationProvider
import io.ktor.server.auth.jwt.JWTPrincipal
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.innsyn.Configuration.APP_NAME
import no.nav.dagpenger.innsyn.Configuration.properties
import java.net.URI
import java.util.concurrent.TimeUnit

object TokenXFactory {
    @Suppress("ktlint:standard:class-naming")
    private object token_x : PropertyGroup() {
        val well_known_url by stringType
        val client_id by stringType
    }

    private val openIdConfiguration =
        runBlocking {
            httpClient.get(properties[token_x.well_known_url]).body<OpenIdConfiguration>()
        }
    private val clientId = properties[token_x.client_id]
    private val issuer = openIdConfiguration.issuer
    private val jwkProvider: JwkProvider
        get() =
            JwkProviderBuilder(URI(openIdConfiguration.jwksUri).toURL())
                .cached(10, 24, TimeUnit.HOURS) // cache up to 10 JWKs for 24 hours
                .rateLimited(
                    10,
                    1,
                    TimeUnit.MINUTES,
                ) // if not cached, only allow max 10 different keys per minute to be fetched from external provider
                .build()

    fun JWTAuthenticationProvider.Config.tokenx() {
        verifier(jwkProvider, issuer) {
            withAudience(clientId)
        }
        realm = APP_NAME
        validate { credentials ->
            requireNotNull(credentials.payload.claims.pid()) {
                "Token må inneholde fødselsnummer for personen i enten pid claim"
            }

            JWTPrincipal(credentials.payload)
        }
    }
}

object AzureAdFactory {
    @Suppress("ktlint:standard:class-naming")
    private object azure_app : PropertyGroup() {
        val well_known_url by stringType
        val client_id by stringType
    }

    private val openIdConfiguration =
        runBlocking {
            httpClient.get(properties[azure_app.well_known_url]).body<OpenIdConfiguration>()
        }
    private val clientId = properties[azure_app.client_id]
    private val issuer = openIdConfiguration.issuer
    private val jwkProvider: JwkProvider
        get() =
            JwkProviderBuilder(URI(openIdConfiguration.jwksUri).toURL())
                .cached(10, 24, TimeUnit.HOURS) // cache up to 10 JWKs for 24 hours
                .rateLimited(
                    10,
                    1,
                    TimeUnit.MINUTES,
                ) // if not cached, only allow max 10 different keys per minute to be fetched from external provider
                .build()

    fun JWTAuthenticationProvider.Config.azure() {
        verifier(jwkProvider, issuer) {
            withAudience(clientId)
        }
        validate { credentials ->
            JWTPrincipal(credentials.payload)
        }
        realm = APP_NAME
    }
}

private data class OpenIdConfiguration(
    @JsonProperty("jwks_uri")
    val jwksUri: String,
    @JsonProperty("issuer")
    val issuer: String,
    @JsonProperty("token_endpoint")
    val tokenEndpoint: String,
    @JsonProperty("authorization_endpoint")
    val authorizationEndpoint: String,
)

private val httpClient =
    HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }
