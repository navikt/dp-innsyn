plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.4.10"
    id("com.diffplug.spotless") version "8.10.1"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.10.0")
}
