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
import org.gradle.api.internal.file.collections.ImmutableFileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaExecSpec
import org.gradle.process.internal.ExecException
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Logging
import org.jooq.meta.jaxb.OnError
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Gradle task for executing jOOQ code generation.
 *
 * @property config jOOQ code generation Configuration
 * @property jooqClassPath list of jars to add to the classpath when running the jOOQ code generation
 * @property outputDirectory directory the jOOQ code generation XML configuration file is generated into
 * @property runConfig configuration for the execution environment for the jOOQ code generation process
 * @property resultHandler handler for the result of the jOOQ code generation process
 */
open class JooqTask @Inject constructor() : DefaultTask(), JooqConfig {

    @get:Internal
    internal var runConfigLambda: (() -> (JavaExecSpec.() -> Unit)?)? = null

    @get:Internal
    internal var resultHandlerLambda: (() -> (JavaExecResult.() -> Unit)?)? = null

    @get:Input
    @get:Optional
    override var runConfig: (JavaExecSpec.() -> Unit)?
        get() = runConfigLambda?.invoke()
        set(value) {
            runConfigLambda = { value }
        }

    @get:Input
    @get:Optional
    override var resultHandler: (JavaExecResult.() -> Unit)?
        get() = resultHandlerLambda?.invoke()
        set(value) {
            resultHandlerLambda = { value }
        }

    @get:Input
    @get:Optional
    override var jdbc: Jdbc?
        get() = config.jdbc
        set(value) {
            config.jdbc = value
        }

    @get:Input
    @get:Optional
    override var generator: Generator?
        get() = config.generator
        set(value) {
            config.generator = value
        }

    @get:Input
    @get:Optional
    override var logging: Logging?
        get() = config.logging
        set(value) {
            config.logging = value
        }

    @get:Input
    override var onError: OnError?
        get() = config.onError
        set(value) {
            config.onError = value
        }

    @get:Input
    override var config: Configuration = Configuration()

    @get:InputFiles
    @get:Classpath
    var jooqClassPath: FileCollection = project.jooqRuntime

    @get:Optional
    @get:OutputDirectory
    val outputDirectory: File? by lazy {
        outputDirName?.let { dirName ->
            File(dirName).let { dir ->
                if (dir.isAbsolute) dir else project.file(dirName)
            }
        }
    }

    private val jooqVersion: JooqVersion
        get() = project.jooqExt.jooqVersion

    private val outputDirName by lazy { config.generator?.target?.directory }

    private val isGeneratorSpecified: Boolean by lazy { config.generator != Configuration().generator }

    init {
        description = JOOQ_TASK_DESC
        group = JOOQ_TASK_GROUP
        onlyIf {
            val hasGeneratorInfo = isGeneratorSpecified
            if (!hasGeneratorInfo) {
                logger.info("jOOQ codegen generator configuration not specified")
            }
            hasGeneratorInfo
        }
    }

    /**
     * Invokes the jOOQ code generator based on the configuration from Gradle.
     */
    @TaskAction
    fun generate() {
        logger.info("jooqRuntime classpath: ${jooqClassPath.files}")
        val configFile = createJooqConfigFile()
        val errorLog = File(temporaryDir, "error_msg_log.txt")
        val logFile = createLoggingConfigFile(errorLog)
        val javaExecResult = try {
            val result = project.javaexec { spec ->
                config.supplementByVersion(jooqVersion, project.logger)
                spec.main = getGenerationTool(jooqVersion)
                spec.classpath = jooqClassPath.plus(ImmutableFileCollection.of(logFile.parentFile))
                spec.args = listOf(configFile.absolutePath)
                spec.workingDir = project.projectDir
                spec.systemProperties = getJooqProps()
                runConfig?.invoke(spec)
            }
            JavaExecResult(result = result)
        } catch (e: ExecException) {
            JavaExecResult(exception = e, errorMsgLog = errorLog.readText())
        }
        javaExecResult.printMsg()
        resultHandler?.invoke(javaExecResult)
        javaExecResult.exception?.let {
            throw JooqTaskException(
                cause = it,
                msg = javaExecResult.errorMsg
            )
        }
    }

    private fun getJooqProps(): Map<String, *> {
        val (prefixToFind, prefixToRemove) = if (name == JOOQ_TASK_NAME)
            JOOQ_PROP_PREFIX to "" else "$name.$JOOQ_PROP_PREFIX" to "$name."
        return System.getProperties().filter { (key, _) ->
            key.toString().startsWith("$prefixToFind.")
        }.map { (key, value) ->
            key.toString().removePrefix(prefixToRemove) to value
        }.toMap().also {
            logger.info("System properties: $it")
        }
    }

    private fun JavaExecResult.printMsg() {
        if (!isSuccess) {
            logger.error("An error occurred invoking the jOOQ code generation plugin: $errorMsg")
        } else {
            logger.info("The jOOQ code generation plugin finished without errors")
        }
    }

    private fun createJooqConfigFile(): File {
        val configFile = File(temporaryDir, JOOQ_CONFIG_NAME)
        logger.info("jOOQ config XML path ${configFile.path}")
        FileOutputStream(configFile).use {
            config.marshall(it)
        }
        logger.debug("jOOQ config file contents:\n${configFile.readText()}")
        return configFile
    }

    private fun createLoggingConfigFile(errorLog: File): File {
        val logFile = File(temporaryDir, "logback.xml")
        logFile.writeText(logbackConfig(errorLog))
        return logFile
    }

    private fun logbackConfig(errorLog: File): String = """
        |<configuration>
        |   <statusListener class="ch.qos.logback.core.status.NopStatusListener" />
        |   <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        |       <encoder>
        |           <pattern>%.-1level %-25.30logger{20} - %msg%n</pattern>
        |       </encoder>
        |   </appender>
        |   <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        |       <file>${errorLog.absolutePath}</file>
        |       <append>false</append>
        |       <immediateFlush>true</immediateFlush>
        |       <encoder>
        |           <pattern>%msg%n%nopex</pattern>
        |       </encoder>
        |       <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        |         <level>ERROR</level>
        |       </filter>
        |   </appender>
        |   <root level="${logging.logbackLevel}">
        |       <appender-ref ref="CONSOLE"/>
        |       <appender-ref ref="FILE"/>
        |   </root>
        |</configuration>""".trimMargin()

    private val Logging?.logbackLevel: String
        get() = when (this) {
            null -> "info"
            Logging.FATAL -> "error"
            else -> name.toLowerCase()
        }

}
