package dev.bombinating.gradle.jooq

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream
import kotlin.streams.asStream

abstract class TestConfigProvider(private val config: TestConfig) : ArgumentsProvider {

    private val editions = listOf(JooqEdition.OpenSource, JooqEdition.Pro, null)
    protected open val versions = listOf(jooqVersion11, jooqVersion12, null)

    private val applicableEditions: List<JooqEdition?>
        get() = editions.filter { edition ->
            (edition?.oss ?: true) ||
                    ((config.edition?.pro ?: false) && (proTestsEnabledValue == System.getenv(envVarProTests)))
        }

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> =
        applicableEditions.flatMap { edition ->
            versions.map { version ->
                config.copy(edition = edition, version = version)
            }
        }.map { Arguments.of(it) }.asSequence().asStream()

}