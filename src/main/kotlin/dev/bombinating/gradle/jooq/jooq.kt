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

/*
 * Internal configuration values and functionality for the plugin.
 */

package dev.bombinating.gradle.jooq

import mu.KotlinLogging
import org.gradle.api.Project
import org.jooq.meta.jaxb.Configuration
import java.io.OutputStream
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

internal const val DEFAULT_JOOQ_VERSION = "3.12.3"
internal val DEFAULT_JOOQ_EDITION = JooqEdition.OpenSource
internal const val JOOQ_RUNTIME_NAME = "jooqRuntime"
internal const val JOOQ_CONFIG_NAME = "config.xml"
internal const val JOOQ_RUNTIME_DESC = "The classpath for the jOOQ generator"
internal const val JOOQ_TASK_GROUP = "jooq"
internal const val JOOQ_EXT_NAME = "jooq"
internal const val JOOQ_TASK_NAME = "jooq"
internal const val JOOQ_TASK_DESC = "jOOQ code generator"
internal const val JOOQ_PROP_PREFIX = "jooq"
internal const val SPRING_DEP_MAN_PLUGIN_NAME = "io.spring.dependency-management"
internal const val SPRING_DEP_MAN_JOOQ_VERSION_EXT_NAME = "jooq.version"
internal const val GRADLE_EXT_EXT_NAME = "ext"

internal val pluginLogger = KotlinLogging.logger("dev.bombinating.gradle.jooq.JooqPlugin")

/*
 * The Spring Dependency Plugin does not manipulate dependencies where the version is specified using "dynamic" notation
 * -- square brackets below (see: https://docs.gradle.org/current/userguide/declaring_dependencies.html). Note that
 * since there is a single version in the range, the version isn't really dynamic.
 */
internal val JooqExtension.codeGenDeps: List<String>
    get() = listOf(
        "${edition.groupId}:jooq:[${version}]",
        "${edition.groupId}:jooq-codegen:[${version}]",
        "${edition.groupId}:jooq-meta:[${version}]",
        "${edition.groupId}:jooq-meta-extensions:[${version}]",
        "javax.xml.bind:jaxb-api:2.3.1",
        "javax.activation:activation:1.1.1",
        "com.sun.xml.bind:jaxb-core:2.3.0.1",
        "com.sun.xml.bind:jaxb-impl:2.3.0.1",
        "org.slf4j:slf4j-api:1.7.28",
        "ch.qos.logback:logback-classic:1.2.3"
    )

internal const val PRE_VERSION_11_GEN_PACKAGE = "org.jooq.util"
internal const val PRE_VERSION_11_GEN_NAME = "$PRE_VERSION_11_GEN_PACKAGE.JavaGenerator"
internal const val VERSION_FF_11_GEN_PACKAGE = "org.jooq.codegen"
internal const val GEN_TOOL = "GenerationTool"

internal val JOOQ_GROUP_IDS = JooqEdition.values().map { it.groupId }.toSet()

internal fun getGenerationTool(jooqVersion: JooqVersion) =
    "${if (jooqVersion < JOOQ_3_11) PRE_VERSION_11_GEN_PACKAGE else VERSION_FF_11_GEN_PACKAGE}.$GEN_TOOL"

internal fun Configuration.supplementByVersion(jooqVersion: JooqVersion) {
    if (jooqVersion < JOOQ_3_11 && generator.name.startsWith(VERSION_FF_11_GEN_PACKAGE)) {
        pluginLogger.info { "Changing Configuration.generator.name from '${generator.name}' to '$PRE_VERSION_11_GEN_NAME'" }
        generator.name = PRE_VERSION_11_GEN_NAME
    }
}

internal fun Configuration.marshall(dest: OutputStream) {
    val marshaller = JAXBContext.newInstance(Configuration::class.java).createMarshaller().apply {
        setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
    }
    marshaller.marshal(this, dest)
}

internal val Project.jooqRuntime: org.gradle.api.artifacts.Configuration
    get() = configurations.getByName(JOOQ_RUNTIME_NAME)

internal val Project.jooqExt: JooqExtension
    get() = this.extensions.getByName(JOOQ_EXT_NAME) as JooqExtension
