package dev.bombinating.gradle.jooq

import mu.KotlinLogging
import org.gradle.testfixtures.ProjectBuilder
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Logging
import org.junit.jupiter.api.Test
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private val logger = KotlinLogging.logger {}
class ConfigTest {

    @Test
    fun f() {
        val project = ProjectBuilder.builder().build()
        val ext = project.configurations.create("jooq")
        /*val task =*/ project.tasks.create("jooq", JooqTask::class.java, Configuration(), ext)
//        val plugin = JooqTask(config = Configuration(), jooqClassPath = FileTreeAdapter(DefaultSingletonFileTree(File("temp"))))
//        plugin.
    }

    @Test
    fun g() {
        val config = config {
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
        val input = ByteArrayInputStream(output.toByteArray())
        val dbf = DocumentBuilderFactory.newInstance()
        val builder = dbf.newDocumentBuilder()
        val xmlDoc = builder.parse(input) //, "http://www.jooq.org/xsd/jooq-codegen-3.11.0.xsd")
//        val xpath = XPathFactory.newInstance().newXPath()
//        val node = xpath.compile("/configuration/logging").evaluate(xmlDoc, XPathConstants.NODE) as Node
//        println("-----> ${node.textContent}")
        xmlDoc.assertXPath("/configuration/logging", "DEBUG")
        xmlDoc.assertXPath("/configuration/jdbc/driver", "org.postgresql.Driver")
        xmlDoc.assertXPath("/configuration/generator/target/directory", "/home/test/gen")
        println(output.toString())
    }

    fun Document.assertXPath(xpathQuery: String, expectedValue: String) {
        logger.info { "xpath=$xpathQuery" }
        val xpath = XPathFactory.newInstance().newXPath()
        val node = xpath.compile(xpathQuery).evaluate(this, XPathConstants.NODE) as Node?
        assertNotNull(node)
        assertEquals(node.textContent, expectedValue)
    }

}
