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
    mainClass.set("no.nav.dagpenger.innsyn.AppKt")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(RapidAndRivers)

    implementation(Ktor.library("auth"))
    implementation(Ktor.library("auth-jwt"))
    implementation(Ktor.library("client-cio"))
    implementation(Ktor.library("client-jackson"))
    implementation(Ktor.library("jackson"))
    implementation(Ktor.library("server-cio"))
    implementation(Ktor.library("websockets"))

    implementation(Konfig.konfig)
    implementation(Kotlin.Logging.kotlinLogging)

    testImplementation(kotlin("test"))
    testImplementation(Junit5.api)
    testImplementation(Ktor.library("server-test-host"))
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
    dependsOn("spotlessApply")
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf("Main-Class" to application.mainClass.get()))
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
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
// Gjør det mulig å kjøre docker-compose up && ./gradlew run
tasks.withType<JavaExec> {
    environment(
        "KAFKA_RAPID_TOPIC" to "private-dagpenger-behov-v2",
        "KAFKA_BOOTSTRAP_SERVERS" to "localhost:9092",
        "KAFKA_CONSUMER_GROUP_ID" to "dp-innsyn",
        "KAFKA_RESET_POLICY" to "earliest"
    )
}
