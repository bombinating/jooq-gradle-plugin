package dev.bombinating.gradle.jooq

import java.io.File
import java.nio.file.Path

fun jooqOsDependency(group: String, version: String) = """compile(group = "$group", name = "jooq", version = "$version")"""

fun dependenciesBlock(jooqDependency: String, jdbcDriverDependency: String) = """
    |   $jooqDependency
    |   compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    |   jooqRuntime($jdbcDriverDependency)
""".trimMargin("|")

fun String.packageToPath() = replace(".", "/")

fun Path.toFile(child: String) = File(toFile(), child)

fun printGradleInfo(settings: File, build: File) {
    println("""settings.gradle.kts:
                |
                |${settings.readText()}
                |
                |build.gradle.kts:
                |
                |${build.readText()}
                |
            """.trimMargin("|"))
}