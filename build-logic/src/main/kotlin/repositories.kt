import dev.kmpx.gradle.core.dsl.utils.mavenCentralSnapshots
import org.gradle.api.artifacts.dsl.RepositoryHandler

fun RepositoryHandler.configureRepositories() {
    mavenCentral()
    mavenCentralSnapshots()
}
