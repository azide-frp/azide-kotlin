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
        commonMain.dependencies {
            implementation(libs.kmpx.collections)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
