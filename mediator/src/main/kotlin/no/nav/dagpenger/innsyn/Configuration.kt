package no.nav.dagpenger.innsyn

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.dagpenger.oauth2.CachedOauth2Client
import no.nav.dagpenger.oauth2.OAuth2Config

internal object Configuration {
    const val APP_NAME = "dp-innsyn"
    private val localProperties =
        ConfigurationMap(
            mapOf(
                "KAFKA_CONSUMER_GROUP_ID" to "dp-innsyn-v1",
                "KAFKA_RAPID_TOPIC" to "private-dagpenger-behov-v2",
                "KAFKA_RESET_POLICY" to "earliest",
                "HTTP_PORT" to "8080",
                "KAFKA_BROKERS" to "localhost:9092",
                "NY_SOKNADSDIALOG_INGRESS" to "https://arbeid.intern.dev.nav.no/dagpenger/dialog/soknad",
                "DP_DATADELING_PERIODER_URL" to "http://localhost:8090/dagpenger/datadeling/v1/perioder",
                "DP_DATADELING_SCOPE" to "api://dev-gcp.teamdagpenger.dp-datadeling/.default",
                "FLYWAY_CLEAN_DISABLED" to "false",
            ),
        )

    private val defaultProperties =
        ConfigurationMap(
            mapOf(
                "KAFKA_CONSUMER_GROUP_ID" to "dp-innsyn-v1",
                "KAFKA_RAPID_TOPIC" to "teamdagpenger.rapid.v1",
                "KAFKA_RESET_POLICY" to "latest",
                "KAFKA_EXTRA_TOPIC" to "teamdagpenger.journalforing.v1,teamdagpenger.arena.oppgave.v1," +
                    "teamarenanais.gg-arena-vedtak-dagpenger-v2-q2",
                "HTTP_PORT" to "8080",
                "DP_DATADELING_PERIODER_URL" to "http://dp-datadeling/dagpenger/datadeling/v1/perioder",
                "DP_DATADELING_SCOPE" to "api://dev-gcp.teamdagpenger.dp-datadeling/.default",
                "FLYWAY_CLEAN_DISABLED" to "true",
            ),
        )

    private val prodProperties =
        ConfigurationMap(
            mapOf(
                "KAFKA_EXTRA_TOPIC" to "teamdagpenger.journalforing.v1,teamdagpenger.arena.oppgave.v1," +
                    "teamarenanais.gg-arena-vedtak-dagpenger-v2-p",
                "DP_DATADELING_PERIODER_URL" to "http://dp-datadeling/dagpenger/datadeling/v1/perioder",
                "DP_DATADELING_SCOPE" to "api://prod-gcp.teamdagpenger.dp-datadeling/.default",
            ),
        )

    val properties by lazy {
        val envProperties = systemProperties() overriding EnvironmentVariables()
        when (envProperties.getOrNull(Key("NAIS_CLUSTER_NAME", stringType))) {
            null -> envProperties overriding localProperties
            "prod-gcp" -> envProperties overriding prodProperties overriding defaultProperties
            else -> envProperties overriding defaultProperties
        }
    }

    val nySøknadsdialogIngress by lazy { properties[Key("NY_SOKNADSDIALOG_INGRESS", stringType)] }
    val dpDatadelingPerioderUrl by lazy { properties[Key("DP_DATADELING_PERIODER_URL", stringType)] }
    val dpDatadelingScope by lazy { properties[Key("DP_DATADELING_SCOPE", stringType)] }
    val azureAdOauth2Client by lazy {
        OAuth2Config.AzureAd(properties).let { azureAdConfig ->
            CachedOauth2Client(azureAdConfig.tokenEndpointUrl, azureAdConfig.privateKey())
        }
    }

    fun asMap(): Map<String, String> =
        properties.list().reversed().fold(emptyMap()) { map, pair ->
            map + pair.second
        }
}
