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

import org.gradle.api.Project
import org.jooq.Constants.XSD_CODEGEN
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import java.io.OutputStream
import javax.xml.XMLConstants
import javax.xml.bind.JAXBContext
import javax.xml.validation.SchemaFactory

internal const val DEFAULT_JOOQ_VERSION = "3.12.1"
internal val DEFAULT_JOOQ_EDITION = JooqEdition.OpenSource
internal const val JOOQ_RUNTIME_NAME = "jooqRuntime"
internal const val JOOQ_CONFIG_NAME = "config.xml"
internal const val JOOQ_RUNTIME_DESC = "The classpath for the jOOQ generator"
internal const val JOOQ_TASK_GROUP = "jooq"
internal const val JOOQ_EXT_NAME = "jooq"
internal const val JOOQ_TASK_NAME = "jooq"
internal const val JOOQ_TASK_DESC = "jOOQ code generator"
internal val JOOQ_CODE_GEN_DEPS = listOf(
    "org.jooq:jooq-codegen:$DEFAULT_JOOQ_VERSION",
    "javax.xml.bind:jaxb-api:2.3.1",
    "javax.activation:activation:1.1.1",
    "com.sun.xml.bind:jaxb-core:2.3.0.1",
    "com.sun.xml.bind:jaxb-impl:2.3.0.1"
)

internal val JOOQ_GROUP_IDS = JooqEdition.values().map { it.groupId }.toSet()

internal fun Configuration.marshall(dest: OutputStream) {
    val factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
    val marshaller = JAXBContext.newInstance(Configuration::class.java).createMarshaller().apply {
        schema = factory.newSchema(GenerationTool::class.java.getResource("/xsd/$XSD_CODEGEN"))
    }
    marshaller.marshal(this, dest)
}

internal val Project.jooqRuntime: org.gradle.api.artifacts.Configuration
    get() = configurations.getByName(JOOQ_RUNTIME_NAME)

internal val Project.jooqExt: JooqExtension
    get() = this.extensions.getByName(JOOQ_EXT_NAME) as JooqExtension