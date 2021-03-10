package no.nav.dagpenger.innsyn

import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.overriding

val config = systemProperties() overriding EnvironmentVariables()

fun main() {
    ApplicationBuilder(System.getenv()).start()
}
