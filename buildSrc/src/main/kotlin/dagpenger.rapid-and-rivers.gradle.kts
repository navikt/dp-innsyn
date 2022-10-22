plugins {
    id("dagpenger.common")
    application
}

repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation(RapidAndRiversKtor2)

    implementation(Konfig.konfig)
    implementation(Kotlin.Logging.kotlinLogging)
}
