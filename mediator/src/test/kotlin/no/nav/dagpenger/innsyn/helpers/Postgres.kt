package no.nav.dagpenger.innsyn.helpers

import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder
import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.db
import org.flywaydb.core.internal.configuration.ConfigUtils
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLContainer.POSTGRESQL_PORT

internal object Postgres {
    private val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:12.0").apply {
            start()
        }
    }

    fun withMigratedDb(block: () -> Unit) {
        withCleanDb {
            PostgresDataSourceBuilder.runMigration()
            block()
        }
    }

    private fun withCleanDb(block: () -> Unit) {
        setup()
        PostgresDataSourceBuilder.clean().run {
            block()
        }.also {
            tearDown()
        }
    }

    private fun setup() {
        System.setProperty(db.host.name, instance.host)
        System.setProperty(
            db.port.name,
            instance.getMappedPort(POSTGRESQL_PORT).toString()
        )
        System.setProperty(db.database.name, instance.databaseName)
        System.setProperty(db.username.name, instance.password)
        System.setProperty(db.password.name, instance.username)
    }

    private fun tearDown() {
        System.clearProperty(db.password.name)
        System.clearProperty(db.username.name)
        System.clearProperty(db.host.name)
        System.clearProperty(db.port.name)
        System.clearProperty(db.database.name)
        System.clearProperty(ConfigUtils.CLEAN_DISABLED)
    }
}
