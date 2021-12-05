plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.6.0"
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("com.diffplug.spotless:spotless-plugin-gradle:5.11.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}
