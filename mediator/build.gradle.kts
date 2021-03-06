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

    implementation(project(":modell"))

    implementation(Database.Flyway)
    implementation(Database.HikariCP)
    implementation(Database.Postgres)
    implementation(Database.Kotlinquery)

    testImplementation(Ktor.library("server-test-host"))
    testImplementation(TestContainers.postgresql)
    testImplementation(Mockk.mockk)
}
