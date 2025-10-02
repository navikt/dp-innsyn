plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.2.20"
    id("com.diffplug.spotless") version "8.0.0"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.0.0")
}
