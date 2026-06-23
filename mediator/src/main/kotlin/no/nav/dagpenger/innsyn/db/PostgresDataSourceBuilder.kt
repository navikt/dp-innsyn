package no.nav.dagpenger.innsyn.db

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.PropertyGroup
import com.natpryce.konfig.booleanType
import com.natpryce.konfig.getValue
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import com.zaxxer.hikari.HikariDataSource
import no.nav.dagpenger.innsyn.Configuration
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val config = ConfigurationProperties.systemProperties() overriding EnvironmentVariables()

internal object PostgresDataSourceBuilder {
    @Suppress("ktlint:standard:class-naming")
    internal object db : PropertyGroup() {
        val host by stringType
        val port by stringType
        val database by stringType
        val username by stringType
        val password by stringType
    }

    val dataSource by lazy {
        HikariDataSource().apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("serverName", config[db.host])
            addDataSourceProperty("portNumber", config[db.port])
            addDataSourceProperty("databaseName", config[db.database])
            addDataSourceProperty("user", config[db.username])
            addDataSourceProperty("password", config[db.password])
            maximumPoolSize = 10
            // Default 30 sekund
            connectionTimeout = 5.seconds.inWholeMilliseconds
            // Default 10 minutter
            idleTimeout = 10.minutes.inWholeMilliseconds
            // Default 2 minutter
            keepaliveTime = 2.minutes.inWholeMilliseconds
            // Default 30 minutter
            maxLifetime = 30.minutes.inWholeMilliseconds
            leakDetectionThreshold = 30.seconds.inWholeMilliseconds
        }
    }

    private val flyWayBuilder: FluentConfiguration =
        Flyway
            .configure()
            .cleanDisabled(Configuration.properties[Key("FLYWAY_CLEAN_DISABLED", booleanType)])
            .connectRetries(5)

    fun clean() = flyWayBuilder.dataSource(dataSource).load().clean()

    internal fun runMigration(initSql: String? = null) =
        flyWayBuilder
            .dataSource(dataSource)
            .initSql(initSql)
            .load()
            .migrate()
}
