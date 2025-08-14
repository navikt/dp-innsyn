plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.2.10"
    id("com.diffplug.spotless") version "7.2.1"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("com.diffplug.spotless:spotless-plugin-gradle:7.2.1")
}
