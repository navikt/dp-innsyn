plugins {
    id(Spotless.spotless) version Spotless.version
}

apply {
    plugin(Spotless.spotless)
}

repositories {
    jcenter()
}

spotless {
    kotlin {
        ktlint(Ktlint.version)
    }
    kotlinGradle {
        target("*.gradle.kts", "buildSrc/**/*.kt*")
        ktlint(Ktlint.version)
    }
}

subprojects {
tasks.named("compileKotlin") {
    println(this)
    dependsOn("spotlessApply")
}
}
