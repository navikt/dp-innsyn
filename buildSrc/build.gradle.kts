plugins {
    `kotlin-dsl`
    //id("com.diffplug.spotless") version "5.11.0"
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

/*spotless {
    kotlin {
        ktlint("0.40.0")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("0.40.0")
    }
}*/