plugins {
    id("dagpenger.rapid-and-rivers")
}

application {
    mainClass.set("no.nav.dagpenger.innsyn.AppKt")
}

dependencies {
    val dpBibliotekerVersion = Dagpenger.Biblioteker.version

    implementation("com.github.navikt.dp-biblioteker:ktor-client-metrics:$dpBibliotekerVersion")
    implementation(Ktor2.Server.library("auth"))
    implementation(Ktor2.Server.library("auth-jwt"))
    implementation(Ktor2.Server.library("cio"))
    implementation(Ktor2.Server.library("call-id"))
    implementation(Ktor2.Server.library("content-negotiation"))
    implementation(Ktor2.Server.library("compression"))
    implementation(Ktor2.Server.library("default-headers"))
    implementation(Ktor2.Server.library("status-pages"))

    implementation(Ktor2.Client.library("content-negotiation"))
    implementation(Ktor2.Client.library("cio"))

    implementation("io.ktor:ktor-serialization-jackson:${Ktor2.version}")

    implementation(project(":modell"))

    implementation(Database.Flyway)
    implementation(Database.HikariCP)
    implementation(Database.Postgres)
    implementation(Database.Kotlinquery)

    implementation(Jackson.core)
    implementation(Jackson.jsr310)
    implementation(Jackson.kotlin)

    implementation("com.github.navikt.dp-biblioteker:oauth2-klient:$dpBibliotekerVersion")

    testImplementation(Ktor2.Client.library("mock"))
    testImplementation(Ktor2.Server.library("test-host"))
    testImplementation(TestContainers.postgresql)
    testImplementation(Mockk.mockk)
}
