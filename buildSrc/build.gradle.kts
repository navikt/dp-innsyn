plugins {
    `kotlin-dsl`
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