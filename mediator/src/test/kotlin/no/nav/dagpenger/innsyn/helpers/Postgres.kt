package no.nav.dagpenger.innsyn.helpers

import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder
import no.nav.dagpenger.innsyn.db.PostgresDataSourceBuilder.db
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLContainer.POSTGRESQL_PORT

internal object Postgres {

    private val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:11.2").apply {
            start()
        }
    }

    fun withMigratedDb(block: () -> Unit) {
        withCleanDb {
            PostgresDataSourceBuilder.runMigration()
            block()
        }
    }

    fun withCleanDb(block: () -> Unit) {
        System.setProperty(db.host.name, instance.host)
        System.setProperty(db.port.name, instance.getMappedPort(POSTGRESQL_PORT).toString())
        System.setProperty(db.database.name, instance.databaseName)
        System.setProperty(db.username.name, instance.username)
        System.setProperty(db.password.name, instance.password)
        PostgresDataSourceBuilder.clean().run {
            block()
        }
    }
}
