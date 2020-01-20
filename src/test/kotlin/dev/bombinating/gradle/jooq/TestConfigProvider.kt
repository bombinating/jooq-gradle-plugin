/*
 * Copyright 2019 Andrew Geery
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.bombinating.gradle.jooq

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream
import kotlin.streams.asStream

abstract class TestConfigProvider(private val config: TestConfig) : ArgumentsProvider {

    private val editions: List<JooqEdition?> =
        listOf(JooqEdition.OpenSource, JooqEdition.Pro, JooqEdition.ProJava8, null)
    protected open val versions: List<String?> = listOf(jooqVersion10, jooqVersion11, jooqVersion12, null)
    protected open val gradleVersions: List<String> = listOf(
        gradleVersion55, gradleVersion56, gradleVersion60, gradleVersion61
    )

    private val applicableEditions: List<JooqEdition?>
        get() = editions.filter { edition ->
            ((config.edition.isPro && edition.isPro) || config.edition.isOss)
                    && (edition.isOss || proTestsEnabled)
        }

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> =
        gradleVersions.flatMap { gradleVersion ->
            applicableEditions.flatMap { edition ->
                versions.mapNotNull { version ->
                    if (edition.isJavaRuntimeSupported(version?.toJooqVersion())
                        && edition.isJooqVersionSupported(version?.toJooqVersion())
                    ) {
                        config.copy(gradleVersion = gradleVersion, edition = edition, version = version)
                    } else null
                }
            }
        }.map { Arguments.of(it) }.asSequence().asStream()

}