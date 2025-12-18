plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.kmpx.gradleCoreApiUtils)
    implementation(libs.kmpx.gradleKotlinApiUtils)
}

gradlePlugin {
    plugins {
        create("build-logic") {
            id = "build-logic"
            implementationClass = ""
        }
    }
}
