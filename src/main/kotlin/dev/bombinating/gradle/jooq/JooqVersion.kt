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

internal const val JOOQ_MAJOR_VERSION = 3
internal const val JOOQ_FIRST_MINOR_VERSION = 1
internal const val JOOQ_GENERATION_TOOL_PACKAGE_CHANGE = 11
internal const val JOOQ_JAVA_9_MINOR_VERSION = 12

internal val JOOQ_3_12 = JooqVersion(JOOQ_MAJOR_VERSION, JOOQ_JAVA_9_MINOR_VERSION)
internal val JOOQ_3_11 = JooqVersion(JOOQ_MAJOR_VERSION, JOOQ_GENERATION_TOOL_PACKAGE_CHANGE)
internal val JOOQ_3_1 = JooqVersion(JOOQ_MAJOR_VERSION, JOOQ_FIRST_MINOR_VERSION)

internal data class JooqVersion(val major: Int, val minor: Int) {

    operator fun compareTo(otherVersion: JooqVersion): Int = when {
        major == otherVersion.major && minor == otherVersion.minor -> 0
        (major > otherVersion.major) || (major == otherVersion.major && minor >= otherVersion.minor) -> 1
        else -> -1
    }

}

internal fun String.toJooqVersion(): JooqVersion {
    val split = split(".")
    return JooqVersion(major = split[0].toInt(), minor = split[1].toInt())
}
