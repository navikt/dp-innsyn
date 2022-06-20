plugins {
    id("dagpenger.common")
    application
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation(RapidAndRivers)

    implementation(Konfig.konfig)
    implementation(Kotlin.Logging.kotlinLogging)
}
