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
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import org.gradle.process.JavaExecSpec
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import java.io.File
import java.io.FileOutputStream
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
open class JooqTask @Inject constructor(
    @get:Input val config: Configuration,
    @get:InputFiles @get:Classpath val jooqClassPath: FileCollection
) : DefaultTask() {

    private val outputDirName by lazy { config.generator.target.directory }

    @Internal
    var runConfig: (JavaExecSpec.() -> Unit)? = null
    @Internal
    var resultHandler: (ExecResult.() -> Unit)? = null

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
        logger.info("jooqRuntime classpath: ${jooqClassPath.files}")
        val result = project.javaexec { spec ->
            val configFile = File(temporaryDir, JOOQ_CONFIG_NAME)
            configFile.parentFile.mkdirs()
            spec.main = GenerationTool::class.java.canonicalName
            spec.classpath = jooqClassPath
            spec.args = listOf(configFile.absolutePath)
            spec.workingDir = project.projectDir
            runConfig?.invoke(spec)
            config.marshall(FileOutputStream(configFile))
        }
        resultHandler?.invoke(result)
    }

}
