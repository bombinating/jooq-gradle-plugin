package dev.bombinating.gradle.jooq

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream
import kotlin.streams.asStream

abstract class TestConfigProvider(private val config: TestConfig) : ArgumentsProvider {

    private val editions = listOf(JooqEdition.OpenSource, JooqEdition.Pro, JooqEdition.ProJava8, null)
    protected open val versions = listOf(jooqVersion11, jooqVersion12, null)

    private val applicableEditions: List<JooqEdition?>
        get() = editions.filter { edition ->
            ((config.edition.isPro && edition.isPro) || config.edition.isOss)
                    && (edition.isOss || proTestsEnabled)
        }

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> =
        applicableEditions.flatMap { edition ->
            versions.mapNotNull { version ->
                if (edition.isJavaRuntimeSupported(version) && edition.isJooqVersionSupported(version)) {
                    config.copy(edition = edition, version = version)
                } else null
            }
        }.map { Arguments.of(it) }.asSequence().asStream()

}