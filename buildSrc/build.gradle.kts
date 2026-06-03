plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.3.21"
    id("com.diffplug.spotless") version "8.6.0"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.5.1")
}
