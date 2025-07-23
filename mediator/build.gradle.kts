plugins {
    id("common")
    application
}

application {
    mainClass.set("no.nav.dagpenger.innsyn.AppKt")
}

dependencies {
    val dpBibliotekerVersion = "2025.07.23-08.30.31e64aee9725"

    implementation(project(":modell"))
    implementation(project(path = ":openapi"))

    implementation(libs.bundles.ktor.server)

    implementation(libs.rapids.and.rivers)
    implementation(libs.konfig)
    implementation(libs.kotlin.logging)
    implementation(libs.bundles.jackson)
    implementation(libs.bundles.postgres)

    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation("no.nav.dagpenger:oauth2-klient:$dpBibliotekerVersion")
    implementation("no.nav.dagpenger:ktor-client-metrics:$dpBibliotekerVersion")

    testImplementation("io.ktor:ktor-client-mock:${libs.versions.ktor.get()}")
    testImplementation("io.ktor:ktor-server-test-host:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-swagger:${libs.versions.ktor.get()}")
    testImplementation(libs.bundles.postgres.test)
    testImplementation(libs.mockk)
    testImplementation(libs.rapids.and.rivers.test)
}
