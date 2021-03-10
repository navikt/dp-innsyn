plugins {
    id("dagpenger.rapid-and-rivers")
}

application {
    mainClass.set("no.nav.dagpenger.innsyn.AppKt")
}

dependencies {
    implementation(Ktor.library("auth"))
    implementation(Ktor.library("auth-jwt"))
    implementation(Ktor.library("client-cio"))
    implementation(Ktor.library("client-jackson"))
    implementation(Ktor.library("jackson"))
    implementation(Ktor.library("server-cio"))
    implementation(Ktor.library("websockets"))

    testImplementation(Ktor.library("server-test-host"))
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
