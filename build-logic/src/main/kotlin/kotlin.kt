import dev.kmpx.gradle.kotlin.dsl.utils.ExperimentalLanguageFeature
import dev.kmpx.gradle.kotlin.dsl.utils.experimentalLanguageFeatures
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// Java 21 is the most recent LTS version
private const val usedJvmToolchainVersion = 21

private val enabledExperimentalCompilerFeatures = listOf(
    ExperimentalLanguageFeature.ConsistentDataClassCopyVisibility,
)

fun KotlinMultiplatformExtension.configureKotlin() {
    jvm()

    js(IR) {
        browser()
        nodejs()
    }

    jvmToolchain(usedJvmToolchainVersion)

    compilerOptions {
        experimentalLanguageFeatures.addAll(enabledExperimentalCompilerFeatures)
    }
}
