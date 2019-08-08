package dev.bombinating.gradle.jooq

import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.gradle.testfixtures.ProjectBuilder
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Logging
import org.junit.jupiter.api.Test
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextInt
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal val Configuration.xJdbc
    get() = withJdbc(jdbc ?: Jdbc()).jdbc
internal val Configuration.xGenerator
    get() = withGenerator(generator ?: Generator()).generator
internal val Generator.xDatabase
    get() = withDatabase(database ?: Database()).database

internal interface ConfigInfo<in T> {
    val objSetter: Configuration.(T) -> Unit
    val dslSetter: Configuration.(T) -> Unit
}

internal data class ConfigInfoGen<T>(val configInfo: () -> List<ConfigInfo<T>>, val generator: () -> T)

internal enum class StringConfigInfo(override val objSetter: Configuration.(String) -> Unit, override val dslSetter: Configuration.(String) -> Unit) : ConfigInfo<String> {
    JdbcDriver({ xJdbc.driver = it }, { jdbc { driver = it } }),
    JdbcUsername({ xJdbc.username = it }, { jdbc { username = it } }),
    JdbcPassword({ xJdbc.password = it }, { jdbc { password = it } }),
    JdbcUrl({ xJdbc.url = it }, { jdbc { url = it } }),
    JdbcUser({ xJdbc.user = it }, { jdbc { user = it } }),
    JdbcSchema({ xJdbc.schema = it }, { jdbc { schema = it } }),
    DbName({ xGenerator.xDatabase.name = it }, { generator { database { name = it } } }),
    DbIncludes({ xGenerator.xDatabase.includes = it }, { generator { database { includes = it } } }),
    DbExcludes({ xGenerator.xDatabase.excludes = it }, { generator { database { excludes = it } } })
    //Property({ withJdbc(jdbc ?: Jdbc()).jdbc.withP = it }, { jdbc { schema = it } }), // FIXME: how to test property
}

internal enum class BooleanConfigInfo(override val objSetter: Configuration.(Boolean) -> Unit, override val dslSetter: Configuration.(Boolean) -> Unit) : ConfigInfo<Boolean> {
    DbIncludeRoutines({ xGenerator.xDatabase.isIncludeRoutines = it }, { generator { database { isIncludeRoutines = it } } }),
    DbDateAsTimestamp({ xGenerator.xDatabase.isDateAsTimestamp = it }, { generator { database { isDateAsTimestamp = it } } }),
    DbForceIntegerTypesOnZeroScaleDecimals({ xGenerator.xDatabase.isForceIntegerTypesOnZeroScaleDecimals = it }, { generator { database { isForceIntegerTypesOnZeroScaleDecimals = it } } }),
    DbIgnoreProcedureReturnValues({ xGenerator.xDatabase.isIgnoreProcedureReturnValues = it }, { generator { database { isIgnoreProcedureReturnValues = it } } }),
}

internal enum class IntConfigInfo(override val objSetter: Configuration.(Int) -> Unit, override val dslSetter: Configuration.(Int) -> Unit) : ConfigInfo<Int> {
    LogSlowerQueriesAfterSecs({ xGenerator.xDatabase.logSlowQueriesAfterSeconds = it }, { generator { database { logSlowQueriesAfterSeconds = it } } })
}

internal enum class ConfigTypes(val type: ConfigInfoGen<*>) {
    STRING(ConfigInfoGen({StringConfigInfo.values().toList()}, { randomAlphanumeric(1, 20) })),
    BOOLEAN(ConfigInfoGen({BooleanConfigInfo.values().toList()}, { nextBoolean() })),
    INT(ConfigInfoGen({IntConfigInfo.values().toList()}, { nextInt() })),
}

private val logger = KotlinLogging.logger {}

class ConfigTest {

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `single property`() {
        ConfigTypes.values().forEach { type ->
            val values = type.type.configInfo().forEach {
                val objConfig = Configuration()
                val dslConfig = Configuration()
                val x = type.type.generator.invoke()
                println("===> ${(it as Enum<*>).name} - $x")
                (it.dslSetter as Configuration.(Any?)-> Unit).invoke(dslConfig, x)
                (it.objSetter as Configuration.(Any?)-> Unit).invoke(objConfig, x)
                assertEquals(objConfig, dslConfig)
            }

        }
    }

    @Test
    fun singlePropertyJdbcTest() {
        StringConfigInfo.values().forEach {
            val objConfig = Configuration()
            val dslConfig = Configuration()
            val s = randomAlphanumeric(1, 20)
            logger.info { "s=$s" }
            it.objSetter.invoke(objConfig, s)
            it.dslSetter.invoke(dslConfig, s)
            assertEquals(objConfig, dslConfig)
        }
    }

    @Test
    fun multiPropertyJdbcTest() {

        val all = StringConfigInfo.values()
        val size = all.size
        val x = nextInt(StringConfigInfo.values().size)
        val list = (0..x).map { all[nextInt(size)] }.distinct()

        val map: MutableMap<StringConfigInfo, String> = mutableMapOf()

        val objConfig = Configuration()
        val dslConfig = Configuration()

        list.forEach {
            val s = randomAlphanumeric(1, 20)
            logger.info { "s=$s" }
            println("s=$s")
            it.objSetter.invoke(objConfig, s)
            it.dslSetter.invoke(dslConfig, s)
            map[it] = s
        }
        assertEquals(objConfig, dslConfig)
    }

    @Test
    fun singlePropertyDatabaseTest() {
        StringConfigInfo.values().forEach {
            val objConfig = Configuration()
            val dslConfig = Configuration()
            val s = "xxxxxxxxx"
            it.objSetter.invoke(objConfig, s)
            it.dslSetter.invoke(dslConfig, s)
            assertEquals(objConfig, dslConfig)
        }
    }

    @Test
    fun f() {
        val project = ProjectBuilder.builder().build()
        val ext = project.configurations.create("jooq")
        /*val task =*/ project.tasks.create("jooq", JooqTask::class.java, Configuration(), ext)
//        val plugin = JooqTask(config = Configuration(), jooqClassPath = FileTreeAdapter(DefaultSingletonFileTree(File("temp"))))
//        plugin.
    }

    // FIXME: do I have to actually even generate a file? can't I just verify that the ext methods do in fact create the proper values in the Configuration object?
    // then we don't have to worry about the case where the conifg isn't actually valid
    @Test
    fun g() {
        val config = config {
            logging = Logging.DEBUG
            jdbc {
                //driver = "org.postgresql.Driver"
                username = "admin"
                password = "password"
                url = "jdbc:postgresql://test"
//                this.properties = mutableListOf(
//                    Properties()
//                )
                //user = "blah"
                /*
                schema
                properties
                 */
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
                this.strategy {
                    matchers {
                        tables {
                            table {

                            }
                        }
                    }
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
        //xmlDoc.assertXPath("/configuration/jdbc/driver", "org.postgresql.Driver")
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
