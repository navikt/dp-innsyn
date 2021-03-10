import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dagpenger.common")
    id(Spotless.spotless) version Spotless.version
}

allprojects {
    apply(plugin = Spotless.spotless)

    spotless {
        kotlin {
            ktlint(Ktlint.version)
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint(Ktlint.version)
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        dependsOn("spotlessApply")
    }
}
