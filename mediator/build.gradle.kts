plugins {
    id("dagpenger.rapid-and-rivers")
}

application {
    mainClass.set("no.nav.dagpenger.innsyn.AppKt")
}

dependencies {
    implementation("com.github.navikt.dp-biblioteker:ktor-client-metrics:2022.06.02-09.13.7b5fc99c5517")
    implementation(Ktor2.Server.library("auth"))
    implementation(Ktor2.Server.library("auth-jwt"))
    implementation(Ktor2.Server.library("core"))
    implementation(Ktor2.Server.library("call-id"))
    implementation(Ktor2.Server.library("call-logging"))
    implementation(Ktor2.Server.library("content-negotiation"))
    implementation(Ktor2.Server.library("compression"))
    implementation(Ktor2.Server.library("cio"))
    implementation(Ktor2.Server.library("default-headers"))
    implementation(Ktor2.Server.library("metrics-micrometer"))
    implementation(Ktor2.Server.library("status-pages"))
    implementation("io.ktor:ktor-serialization-jackson:${Ktor2.version}")
    implementation(Ktor2.Client.library("cio"))
    implementation(Ktor2.Client.library("content-negotiation"))

    implementation(project(":modell"))

    implementation(Database.Flyway)
    implementation(Database.HikariCP)
    implementation(Database.Postgres)
    implementation(Database.Kotlinquery)

    implementation(Jackson.core)
    implementation(Jackson.jsr310)
    implementation(Jackson.kotlin)

    implementation("com.github.navikt.dp-biblioteker:oauth2-klient:2022.05.30-09.37.623ee13a49dd")

    testImplementation(Ktor2.Client.library("mock"))
    testImplementation(Ktor2.Server.library("test-host"))
    testImplementation(TestContainers.postgresql)
    testImplementation(Mockk.mockk)
}
