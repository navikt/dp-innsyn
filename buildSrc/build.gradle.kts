plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.3.10"
    id("com.diffplug.spotless") version "8.2.1"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.2.1")
}
