import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version Kotlin.version
    application
    id(Spotless.spotless) version Spotless.version
}

apply {
    plugin(Spotless.spotless)
}

repositories {
    jcenter()
    maven("https://jitpack.io")
}

application {
    mainClass.set("no.nav.dagpenger.innsyn")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(RapidAndRivers)

    implementation(Konfig.konfig)
    implementation(Kotlin.Logging.kotlinLogging)

    testImplementation(kotlin("test"))
    testImplementation(Junit5.api)
    testRuntimeOnly(Junit5.engine)
}

spotless {
    kotlin {
        ktlint(Ktlint.version)
    }
    kotlinGradle {
        target("*.gradle.kts", "buildSrc/**/*.kt*")
        ktlint(Ktlint.version)
    }
}

tasks.named("compileKotlin") {
    dependsOn("spotlessCheck")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}
