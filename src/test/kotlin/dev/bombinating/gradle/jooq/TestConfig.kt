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

private val unspecifiedJooqVersion = defaultJooqVersion.toJooqVersion()

val JooqEdition?.isPro: Boolean
    get() = this?.pro ?: DEFAULT_JOOQ_EDITION.pro

val JooqEdition?.isOss: Boolean
    get() = !isPro

internal fun JooqEdition?.isJavaRuntimeSupported(jooqVersion: JooqVersion?): Boolean =
    this?.javaRuntimeSupported(jooqVersion ?: unspecifiedJooqVersion)
        ?: DEFAULT_JOOQ_EDITION.javaRuntimeSupported(jooqVersion ?: unspecifiedJooqVersion)

internal fun JooqEdition?.isJooqVersionSupported(jooqVersion: JooqVersion?): Boolean =
    this?.jooqVersionSupported(jooqVersion ?: unspecifiedJooqVersion)
        ?: DEFAULT_JOOQ_EDITION.jooqVersionSupported(jooqVersion ?: unspecifiedJooqVersion)

data class TestConfig(
    val driver: String,
    val url: String? = null,
    val username: String? = null,
    val password: String? = null,
    val schema: String,
    val genDir: String,
    val javaVersion: String,
    val packageName: String,
    val edition: JooqEdition? = null,
    val version: String? = null,
    val dbGenerator: String,
    val addSchemaToPackage: Boolean = true,
    val additionalPlugins: String? = null,
    val additionalConfig: String? = null
) {

    override fun toString(): String =
        "edition: ${edition ?: "<not specified>"}, version: ${version ?: "<not specified>"}"

}