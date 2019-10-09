package dev.bombinating.gradle.jooq

import java.io.File
import java.nio.file.Path

fun Path.createPropFile() {
    File(toFile(), "gradle.properties").also {
        it.writeText(createPropContent())
    }
}

fun createPropContent(): String = """
    |$envVarJooqRepoUrl=${System.getenv(envVarJooqRepoUrl) ?: ""}
    |$envVarJooqRepoUsername=${System.getenv(envVarJooqRepoUsername) ?: ""}
    |$envVarJooqRepoPassword=${System.getenv(envVarJooqRepoPassword) ?: ""}
""".trimMargin("|")