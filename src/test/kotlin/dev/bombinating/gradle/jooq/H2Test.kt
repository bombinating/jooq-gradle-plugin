package dev.bombinating.gradle.jooq

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.sql.DriverManager

class H2Test {

    companion object {

        private val URL = "jdbc:h2:~/test_db;AUTO_SERVER=true"
        private val USER = "sa"
        private val PASSWORD = ""
        private val SCHEMA = "test"

        @BeforeAll
        @JvmStatic
        fun setup() {
            DriverManager.getConnection(URL, USER, PASSWORD).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("create schema if not exists $SCHEMA")
                    stmt.execute("create table if not exists $SCHEMA.acme(id int)")
                }
            }
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            DriverManager.getConnection(URL, USER, PASSWORD).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.execute("SHUTDOWN")
                }
            }
        }

    }

    @TempDir
    lateinit var workspaceDir: Path

//    @Test
//    fun taskTest() {
//        val x = """
//            import dev.bombinating.gradle.jooq.*
//        import org.jooq.meta.jaxb.Logging
//
//        val genDir = "$projectDir/generated/src/main/java"
//
//        plugins {
//            java
//            id("dev.bombinating.jooq-codegen") version "2.0.2-SNAPSHOT"
//        }
//
//        sourceSets["main"].java {
//            srcDir(genDir)
//        }
//
//        group = "com.acme"
//        version = "1.0-SNAPSHOT"
//
//        repositories {
//            mavenLocal()
//            mavenCentral()
//        }
//
//        dependencies {
//            compile(group = "org.jooq", name = "jooq", version = "3.12.1")
//    compile("javax.annotation:javax.annotation-api:1.3.2")
//    jooqRuntime("com.h2database:h2:1.4.199")
//        }
//
//        configure<JavaPluginConvention> {
//            sourceCompatibility = JavaVersion.VERSION_1_8
//        }
//
//        tasks.create<JooqTask> {
//
//                    jdbc {
//                        driver = "org.h2.Driver"
//                        url = "jdbc:h2:~/test_db;AUTO_SERVER=true"
//                        user = "sa"
//                        password = ""
//                    }
//                    generator {
//                        database {
//                            name = "org.jooq.meta.h2.H2Database"
//                            includes = ".*"
//                        }
//                        target {
//                            directory = genDir
//                            packageName = "com.acme.domain.generated"
//                        }
//                    }
//                    logging = Logging.TRACE
//
//        }
//        """.trimIndent()
//    }

    @Test
    fun baseTestNew() {
        val confName = "h2"
        workspaceDir.createSettings()
        workspaceDir.createBuild(
            genDir = GEN_DIR,
            depBlock = """
                compile(group = "org.jooq", name = "jooq", version = "$JOOQ_VERSION")
                compile("javax.annotation:javax.annotation-api:1.3.2")
                jooqRuntime("com.h2database:h2:1.4.199")""".trimIndent()
        ) {
            """
                jdbc {
                    driver = "org.h2.Driver"
                    url = "$URL"
                    user = "$USER"
                    password = "$PASSWORD"
                }
                generator {
                    database {
                        name = "org.jooq.meta.h2.H2Database"
                        includes = ".*"
                    }
                    target {
                        directory = genDir
                        packageName = "$PACKAGE_NAME"
                    }
                }
                logging = Logging.TRACE
            """
        }

        val dir = workspaceDir.toFile()
        val xxx = File(dir, "build.gradle.kts").readText()
        println("xxx=$xxx")

        val result = GradleRunner.create()
            .withPluginClasspath()
            .withArguments("jooq", "build")
            .withProjectDir(dir)
            .forwardOutput()
            .build()
        assertTrue(
            File(
                workspaceDir.toFile(),
                "$GEN_DIR/${PACKAGE_NAME.replace(".", "/")}/test/tables/Acme.java"
            ).exists()
        )
        //assertTrue(result.task(":generate${confName.capitalize()}Jooq") != null)
        assertTrue(result.task(":jooq") != null)
        assertEquals(TaskOutcome.SUCCESS, result.task(":jooq")?.outcome)
    }


    //@Test
    fun baseTest() {
        val confName = "h2"
        workspaceDir.createSettings()
        workspaceDir.createBuild(
            genDir = GEN_DIR,
            depBlock = """
                compile(group = "org.jooq", name = "jooq", version = "$JOOQ_VERSION")
                compile("javax.annotation:javax.annotation-api:1.3.2")
                jooqRuntime("com.h2database:h2:1.4.199")""".trimIndent()
        ) {
            """
            "$confName"(sourceSets["main"]) {
                jdbc {
                    driver = "org.h2.Driver"
                    url = "$URL"
                    user = "$USER"
                    password = "$PASSWORD"
                }
                generator {
                    database {
                        name = "org.jooq.meta.h2.H2Database"
                        includes = ".*"
                    }
                    target {
                        directory = genDir
                        packageName = "$PACKAGE_NAME"
                    }
                }
                logging = Logging.TRACE
            }
            """.trimIndent()
        }

        val result = GradleRunner.create()
            .withPluginClasspath()
            .withArguments("build")
            .withProjectDir(workspaceDir.toFile())
            .forwardOutput()
            .build()
        assertTrue(
            File(
                workspaceDir.toFile(),
                "$GEN_DIR/${PACKAGE_NAME.replace(".", "/")}/test/tables/Acme.java"
            ).exists()
        )
        assertTrue(result.task(":generate${confName.capitalize()}Jooq") != null)
        assertEquals(TaskOutcome.SUCCESS, result.task(":generate${confName.capitalize()}Jooq")!!.outcome)
    }

    //@Test
    fun testNoSourceSetBuild() {
        val confName = "h2"
        workspaceDir.createSettings()
        workspaceDir.createBuild(
            pluginVersion = "2.0.1-SNAPSHOT",
            genDir = GEN_DIR,
            depBlock = """
                compile(group = "org.jooq", name = "jooq", version = "$JOOQ_VERSION")
                compile("javax.annotation:javax.annotation-api:1.3.2")
                jooqRuntime("com.h2database:h2:1.4.199")""".trimIndent()
        ) {
            """
            "$confName" {
                jdbc {
                    driver = "org.h2.Driver"
                    url = "$URL"
                    user = "$USER"
                    password = "$PASSWORD"
                }
                generator {
                    database {
                        name = "org.jooq.meta.h2.H2Database"
                        includes = ".*"
                    }
                    target {
                        directory = genDir
                        packageName = "$PACKAGE_NAME"
                    }
                }
                logging = Logging.TRACE
            }
            """.trimIndent()
        }

        val result = GradleRunner.create()
            .withPluginClasspath()
            .withArguments("build")
            .withProjectDir(workspaceDir.toFile())
            .forwardOutput()
            .build()
        assertNull(result.task(":generate${confName.capitalize()}Jooq"))
    }

    //@Test
    fun testNoSourceSetGenerateJooq() {
        val confName = "h2"
        workspaceDir.createSettings()
        workspaceDir.createBuild(
            pluginVersion = "2.0.1-SNAPSHOT",
            genDir = GEN_DIR,
            depBlock = """
                compile(group = "org.jooq", name = "jooq", version = "$JOOQ_VERSION")
                compile("javax.annotation:javax.annotation-api:1.3.2")
                jooqRuntime("com.h2database:h2:1.4.199")""".trimIndent()
        ) {
            """
            "$confName" {
                jdbc {
                    driver = "org.h2.Driver"
                    url = "$URL"
                    user = "$USER"
                    password = "$PASSWORD"
                }
                generator {
                    database {
                        name = "org.jooq.meta.h2.H2Database"
                        includes = ".*"
                    }
                    target {
                        directory = genDir
                        packageName = "$PACKAGE_NAME"
                    }
                }
                logging = Logging.TRACE
            }
            """.trimIndent()
        }

        val result = GradleRunner.create()
            .withPluginClasspath()
            .withArguments("generateH2Jooq",  "build")
            .withProjectDir(workspaceDir.toFile())
            .forwardOutput()
            .build()
        assertTrue(
            File(
                workspaceDir.toFile(),
                "$GEN_DIR/${PACKAGE_NAME.replace(".", "/")}/test/tables/Acme.java"
            ).exists()
        )
        assertTrue(result.task(":generate${confName.capitalize()}Jooq") != null)
        assertEquals(TaskOutcome.SUCCESS, result.task(":generate${confName.capitalize()}Jooq")!!.outcome)
    }

}
