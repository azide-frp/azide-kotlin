plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

repositories {
    configureRepositories()
}

kotlin {
    js(IR) {
        binaries.executable()
        browser()
    }

    jvmToolchain(21)

    configureKotlinCompilerOptions()

    sourceSets {
        jsMain.dependencies {
            implementation(project(":core"))
            implementation(project(":dom"))
            implementation(libs.kmpx.jsApiCompat)
        }
    }
}
