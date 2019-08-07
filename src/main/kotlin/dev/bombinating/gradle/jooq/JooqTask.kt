package dev.bombinating.gradle.jooq

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jooq.Constants
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.xml.XMLConstants
import javax.xml.bind.JAXBContext
import javax.xml.validation.SchemaFactory

open class JooqTask @Inject constructor(
    config: Configuration,
    @get:InputFiles @get:Classpath val jooqClassPath: FileCollection
) : DefaultTask() {

    private val _config: Configuration = config

    private val config: Configuration
            get() = relativeTo(_config, project.projectDir)

    @get:OutputDirectory
    val outputDirectory: File
        get() = project.file(config.generator.target.directory)

    init {
        description = jooqTaskDesc
        group = jooqTaskGroupName
    }

    @TaskAction
    fun generate() {
        execJooq(File(temporaryDir, "config.xml"))
    }

    private fun execJooq(configFile: File) = project.javaexec { spec ->
        spec.main = "org.jooq.codegen.GenerationTool"
        spec.classpath = jooqClassPath
        spec.args = listOf(configFile.absolutePath)
        spec.workingDir = project.projectDir
        configFile.parentFile.mkdirs()
        writeConfig(config, configFile)
    }

    private fun writeConfig(config: Configuration, file: File) {
        val factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        val schema = factory.newSchema(GenerationTool::class.java.getResource("/xsd/${Constants.XSD_CODEGEN}"))
        val ctx = JAXBContext.newInstance(Configuration::class.java)
        val marshaller = ctx.createMarshaller()
        marshaller.schema = schema
        FileOutputStream(file).use { fs ->
            marshaller.marshal(config, fs)
        }
    }

    //  FIXME: is this actually used?!
    private fun relativeTo(config: Configuration, dir: File) =
        config.generator.target.directory?.let {
            val file = File(it)
            if (file.isAbsolute) {
                var relativized = dir.toURI().relativize(file.toURI()).path
                if (relativized.endsWith(File.separator)) {
                    relativized = relativized.substring(0, -2)
                }
                config.generator.target.directory = relativized
            }
            config
        } ?: config

}
