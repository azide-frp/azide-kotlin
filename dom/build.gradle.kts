plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
}

val version = "0.1.0-SNAPSHOT"

repositories {
    configureRepositories()
}

this@Project.group = groupId
this@Project.version = version

kotlin {
    configureKotlin()

    sourceSets {
        jsMain.dependencies {
            implementation(project(":core"))
            implementation(libs.kmpx.jsApiCompat)
        }
    }
}
