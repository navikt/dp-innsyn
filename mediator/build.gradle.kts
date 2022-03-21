plugins {
    id("dagpenger.rapid-and-rivers")
}

application {
    mainClass.set("no.nav.dagpenger.innsyn.AppKt")
}

dependencies {
    // ktor http client
    implementation(Dagpenger.Biblioteker.Ktor.Client.metrics)
    implementation(Ktor.library("auth"))
    implementation(Ktor.library("auth-jwt"))
    implementation(Ktor.library("client-cio"))
    implementation(Ktor.library("client-jackson"))
    implementation(Ktor.library("jackson"))
    implementation(Ktor.library("server-cio"))

    implementation(project(":modell"))

    implementation(Database.Flyway)
    implementation(Database.HikariCP)
    implementation(Database.Postgres)
    implementation(Database.Kotlinquery)

    implementation(Jackson.core)
    implementation(Jackson.jsr310)
    implementation(Jackson.kotlin)

    implementation("com.github.navikt.dp-biblioteker:oauth2-klient:2022.02.05-16.32.da1deab37b31")

    testImplementation(Ktor.library("client-mock"))
    testImplementation(Ktor.library("server-test-host"))
    testImplementation(TestContainers.postgresql)
    testImplementation(Mockk.mockk)
}
