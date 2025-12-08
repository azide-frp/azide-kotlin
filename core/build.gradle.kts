import java.lang.IllegalStateException

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
}

repositories {
    mavenCentral()
}

group = "dev.azide"
version = "0.1.0-SNAPSHOT"

kotlin {
    jvm()

    // Java 21 is the most recent LTS version
    jvmToolchain(21)

    js {
        browser()
        nodejs()
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
