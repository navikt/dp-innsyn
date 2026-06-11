plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.4.0"
    id("com.diffplug.spotless") version "8.6.0"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.6.0")
}
