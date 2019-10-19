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

import org.apache.commons.lang3.JavaVersion
import org.apache.commons.lang3.JavaVersion.JAVA_1_6
import org.apache.commons.lang3.JavaVersion.JAVA_1_8
import org.apache.commons.lang3.JavaVersion.JAVA_9
import org.apache.commons.lang3.SystemUtils

internal data class JooqVersion(val major: Int, val minor: Int) {

    operator fun compareTo(otherVersion: JooqVersion): Int = when {
        major == otherVersion.major && minor == otherVersion.minor -> 0
        (major > otherVersion.major) || (major == otherVersion.major && minor >= otherVersion.minor) -> 1
        else -> -1
    }

}

private const val JOOQ_MAJOR_VERSION = 3
private const val JOOQ_FIRST_MINOR_VERSION = 1
private const val JOOQ_JAVA_9_MINOR_VERSION = 12

private val JOOQ_3_12 = JooqVersion(JOOQ_MAJOR_VERSION, JOOQ_JAVA_9_MINOR_VERSION)
private val JOOQ_3_1 = JooqVersion(JOOQ_MAJOR_VERSION, JOOQ_FIRST_MINOR_VERSION)

/**
 * jOOQ edition info.
 *
 * @property groupId Maven group id associated with the jOOQ edition
 * @property pro whether the version is non-OS
 */
enum class JooqEdition(
    val groupId: String,
    val pro: Boolean,
    private val minJavaVersion: (JooqVersion) -> JavaVersion,
    private val firstJooqVersion: JooqVersion
) {
    /**
     * Open source edition.
     */
    OpenSource("org.jooq", false, { JAVA_1_8 }, JOOQ_3_1),
    /**
     * Java 9+ Pro edition.
     */
    Pro("org.jooq.pro", true, { if (it < JOOQ_3_12) JAVA_1_8 else JAVA_9 }, JOOQ_3_1),
    /**
     * Java 8+ Pro edition.
     */
    ProJava8("org.jooq.pro-java-8", true, { JAVA_1_8 }, JOOQ_3_12),
    /**
     * Java 6+ Pro edition.
     */
    ProJava6("org.jooq.pro-java-6", true, { JAVA_1_6 }, JOOQ_3_1),
    /**
     * Java 6+ Pro edition.
     */
    Trial("org.jooq.trial", true, { JAVA_1_8 }, JOOQ_3_1)
    ;

    fun javaRuntimeSupported(jooqVersion: String): Boolean {
        val version = parseJooqVersion(jooqVersion)
        val minJavaVersion = minJavaVersion(version)
        return SystemUtils.isJavaVersionAtLeast(minJavaVersion)
    }

    fun jooqVersionSupported(jooqVersion: String): Boolean = parseJooqVersion(jooqVersion) >= firstJooqVersion

    private fun parseJooqVersion(jooqVersion: String): JooqVersion {
        val split = jooqVersion.split(".")
        return JooqVersion(major = split[0].toInt(), minor = split[1].toInt())
    }

}
