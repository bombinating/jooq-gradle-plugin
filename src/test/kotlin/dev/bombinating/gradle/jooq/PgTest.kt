package dev.bombinating.gradle.jooq

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.nio.file.Path
import java.sql.DriverManager

@Testcontainers
class PgTest {

    companion object {

        @Container
        private val db = PostgreSQLContainer<Nothing>("postgres:11.2")

        @BeforeAll
        @JvmStatic
        fun setup() {
            println("start")
            DriverManager.getConnection(db.jdbcUrl, db.username, db.password).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("create table andrew(id int)")
                    println("here")
                }
            }
            println("end")
        }

    }

    //val testKitDir = File("build/testkit").absoluteFile
    @TempDir
    lateinit var workspaceDir: Path

    private val packageName = "com.acme.domain.generated"
    private val genDir = "generated/src/main/java"
    private fun tableDir() = File(workspaceDir.toFile(), packageName.replace(".", "/"))

    @Test
    fun f() {
        println("hi!")
        assertTrue(db.isRunning)

        val settingsFile = File(workspaceDir.toFile(), "settings.gradle.kts")
        settingsFile.writeText(createSettings(name = "com.acme"))
        val buildFile = File(workspaceDir.toFile(), "build.gradle.kts")
        buildFile.writeText(createBuild(url = db.jdbcUrl, username = db.username, password = db.password))

        val result = GradleRunner.create()
            .withPluginClasspath()
            .withArguments("build")
            .withProjectDir(workspaceDir.toFile())
            .forwardOutput()
            .build()
        assertTrue(File(workspaceDir.toFile(), "generated/src/main/java/com/acme/domain/generated/tables/Andrew.java").exists())
        assertTrue(result.task(":generatePgJooq") != null)
    }

    fun createSettings(name: String) = """
    rootProject.name = "$name"
    pluginManagement {
        repositories {
            mavenLocal()
            gradlePluginPortal()
        }
    }
    """.trimIndent()

    fun createBuild(url: String, username: String, password: String) = """
    import dev.bombinating.gradle.jooq.config.jdbc
    import dev.bombinating.gradle.jooq.config.generator
    import dev.bombinating.gradle.jooq.config.database
    import dev.bombinating.gradle.jooq.config.target
    import org.jooq.meta.jaxb.Logging
    
    val genDir = "${'$'}projectDir/$genDir"
    
    plugins {
        java
        id("dev.bombinating.jooq") version "0.0.1"
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
        compile(group = "org.jooq", name = "jooq", version = "3.11.11")
        jooqRuntime(group = "org.postgresql", name = "postgresql", version ="42.2.6")
    }
    
    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    jooq {
        "pg"(sourceSets["main"]) {
            jdbc {
                driver = "org.postgresql.Driver"
                url = "$url"
                user = "$username"
                password = "$password"
            }
            generator {
                database {
                    includes = ".*"
                    inputSchema = "public"
                }
                target {
                    directory = genDir
                    packageName = "$packageName"
                }
            }
            logging = Logging.TRACE
        }
    }
    """.trimIndent()
}
