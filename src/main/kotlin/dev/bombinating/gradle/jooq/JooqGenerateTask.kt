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

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Logging
import java.io.File
import java.net.URI
import java.net.URLClassLoader
import javax.inject.Inject

/**
 * Gradle task for executing jOOQ code generation.
 *
 * @property config jOOQ code generation [Configuration]
 * @property jooqClassPath list of jars to add to the classpath when running the jOOQ code generation
 * @property runConfig configuration of how Java should be invoked when the jOOQ code generation tool is executed
 * @property resultHandler lambda to execute when the jOOQ code generation tool is finished
 * @property outputDirectory directory the jOOQ code generation XML configuration file is generated into
 */

// FIXME: modify this so there are no parameters in the constructor
// instead, just set the parameters explicitily

open class JooqGenerateTask @Inject constructor(
    //@get:Input val configuration: Configuration = Configuration(),
    //jooqConfig: JooqConfig = JooqConfigImpl(configuration),
    //jooqClassPath: FileCollection? = null
) : DefaultTask(), JooqConfig { //, JooqConfig by jooqConfig {

    @get:Input
    override var jdbc: Jdbc?
        get() = config.jdbc
        set(value) {
            config.jdbc = value
        }

    @get:Input
    override var generator: Generator?
        get() = config.generator
        set(value) {
            config.generator = value
        }

    @get:Input
    override var logging: Logging?
        get() = config.logging
        set(value) {
            config.logging = value
        }

    @get:Input override var config: Configuration = Configuration()

    //constructor() : this(configuration = Configuration())

    private val outputDirName by lazy { config.generator.target.directory }

    @get:InputFiles
    @get:Classpath
    var jooqClassPath: FileCollection = /*jooqClassPath ?:*/ project.configurations.getByName(JOOQ_RUNTIME_NAME)

    @get:OutputDirectory
    val outputDirectory: File by lazy {
        File(outputDirName).let { dir ->
            if (dir.isAbsolute) dir else project.file(outputDirName)
        }
    }

    init {
        description = JOOQ_TASK_DESC
        group = JOOQ_TASK_GROUP
    }

    @TaskAction
    fun generate() {
        // FIXME: look up the classpath in the jooqSetup extension
        logger.info("jooqRuntime classpath: ${jooqClassPath.files}")
        GenerationTool().apply {
            setClassLoader(
                URLClassLoader("jOOQ ${GenerationTool::class} for codegen",
                    jooqClassPath.files.map(File::toURI).map(URI::toURL).toTypedArray(),
                    project.buildscript.classLoader
                )
            )
            run(config)
        }
    }

}
