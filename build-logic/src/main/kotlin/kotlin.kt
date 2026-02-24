import dev.kmpx.gradle.kotlin.dsl.utils.ExperimentalLanguageFeature
import dev.kmpx.gradle.kotlin.dsl.utils.experimentalLanguageFeatures
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsSubTargetDsl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// Java 21 is the most recent LTS version
private const val usedJvmToolchainVersion = 21

private val enabledExperimentalCompilerFeatures = listOf(
    ExperimentalLanguageFeature.ConsistentDataClassCopyVisibility,
    ExperimentalLanguageFeature.ExpectActualClasses,
    ExperimentalLanguageFeature.ContextParameters,
    ExperimentalLanguageFeature.NestedTypeAliases,
)

val testTimeoutDuration: Duration = 10.seconds

fun KotlinMultiplatformExtension.configureKotlinCompilerOptions() {
    compilerOptions {
        experimentalLanguageFeatures.addAll(enabledExperimentalCompilerFeatures)
    }
}

fun KotlinMultiplatformExtension.configureKotlin() {
    jvm()

    js(IR) {
        browser {
            testTask(
                timeoutDuration = testTimeoutDuration,
            )
        }

        nodejs {
            testTask(
                timeoutDuration = testTimeoutDuration,
            )
        }
    }

    jvmToolchain(usedJvmToolchainVersion)

    configureKotlinCompilerOptions()
}

private fun KotlinJsSubTargetDsl.testTask(
    timeoutDuration: Duration,
) {
    testTask {
        useMocha {
            timeout = "${timeoutDuration.inWholeSeconds}s"
        }
    }
}
