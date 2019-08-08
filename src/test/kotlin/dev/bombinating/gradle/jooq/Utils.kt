package dev.bombinating.gradle.jooq

import java.io.File
import java.nio.file.Path

fun Path.createSettings() = File(toFile(), "settings.gradle.kts").also {
    it.writeText(createSettingsContent())
}

private fun createSettingsContent() = """
    rootProject.name = "com.acme"
    pluginManagement {
        repositories {
            mavenLocal()
            gradlePluginPortal()
        }
    }
    """.trimIndent()

fun Path.createBuild(genDir: String, depBlock: String, pluginVersion: String = "0.0.4-SNAPSHOT", jooqBlock: (String) -> String) = File(toFile(), "build.gradle.kts").also {
    it.writeText(createBuildContent(genDir = genDir, depBlock = depBlock, pluginVersion = pluginVersion, jooqBlock = jooqBlock))
}

/*
compile(group = "org.jooq", name = "jooq", version = "3.11.11")
        jooqRuntime("$jdbcLib")
 */

/*
        "pg"(sourceSets["main"]) {
            jdbc {
                url = "$url"
                user = "$username"
                password = "$password"
            }
            generator {
                database {
                    includes = ".*"
                }
                target {
                    directory = genDir
                    packageName = "$packageName"
                }
            }
            logging = Logging.TRACE
        }
 */
fun createBuildContent(genDir: String, depBlock: String, pluginVersion: String = "0.0.4-SNAPSHOT", jooqBlock: (String) -> String) = """
    import dev.bombinating.gradle.jooq.*
    import org.jooq.meta.jaxb.Logging
    
    val genDir = "${'$'}projectDir/$genDir"
    
    plugins {
        java
        id("dev.bombinating.jooq") version "$pluginVersion"
    }
    
    sourceSets["main"].java {
        srcDir(genDir)
    }
    
    group = "com.acme"
    version = "1.0-SNAPSHOT"
    
    repositories {
        mavenLocal()
        mavenCentral()
    }
    
    dependencies {
        $depBlock
    }
    
    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    jooq {
        ${jooqBlock(genDir)}
    }
    """.trimIndent()
