package dev.bombinating.gradle.jooq

import org.gradle.testfixtures.ProjectBuilder
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Logging
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class TaskTest {

    @Test
    fun f() {
        val project = ProjectBuilder.builder().build()
        val ext = project.configurations.create("jooq")
        val task = project.tasks.create("jooq", JooqTask::class.java, Configuration(), ext)
//        val plugin = JooqTask(config = Configuration(), jooqClassPath = FileTreeAdapter(DefaultSingletonFileTree(File("temp"))))
//        plugin.
    }

    @Test
    fun g() {
        val config = Configuration().apply {
            logging = Logging.DEBUG
            jdbc {
                driver = "org.postgresql.Driver"
                username = "admin"
                password = "password"
                url = "jdbc:postgresql://test"
            }
            generator {
                database {
                    name = "org.jooq.meta.oracle.OracleDatabase"
                    includes = ".*"
                    excludes = "^BIN\\$.*|flyway_schema_history"
                    inputSchema = "TEST"
                    logSlowQueriesAfterSeconds = 10
                }
                target {
                    directory = "/home/test/gen"
                    packageName = "gov.nm.env.csi.domain.generated"
                }
            }
        }
        val output = ByteArrayOutputStream()
        config.marshall(output)
        println(output.toString())
    }

}
