package dev.bombinating.gradle.jooq

import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.junit.jupiter.api.Test
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextInt
import kotlin.test.assertEquals

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

internal data class ConfigInfoGen<T>(val configs: List<ConfigInfo<T>>, val generator: () -> T)

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
    DbIncludeExcludeColumns({ xGenerator.xDatabase.isIncludeExcludeColumns = it }, { generator { database { isIncludeExcludeColumns = it } } }),
    DbIncludeForeignKeys({ xGenerator.xDatabase.isIncludeForeignKeys = it }, { generator { database { isIncludeForeignKeys = it } } }),
    DbIncludeIndexes({ xGenerator.xDatabase.isIncludeIndexes = it }, { generator { database { isIncludeIndexes = it } } })
}

internal enum class IntConfigInfo(override val objSetter: Configuration.(Int) -> Unit, override val dslSetter: Configuration.(Int) -> Unit) : ConfigInfo<Int> {
    LogSlowerQueriesAfterSecs({ xGenerator.xDatabase.logSlowQueriesAfterSeconds = it }, { generator { database { logSlowQueriesAfterSeconds = it } } })
}

internal enum class ConfigTypes(val type: ConfigInfoGen<*>) {
    STRING(ConfigInfoGen(StringConfigInfo.values().toList()) { randomAlphanumeric(1, 20) }),
    BOOLEAN(ConfigInfoGen(BooleanConfigInfo.values().toList()) { nextBoolean() }),
    INT(ConfigInfoGen(IntConfigInfo.values().toList()) { nextInt() })
}

private val logger = KotlinLogging.logger {}

class ConfigTest {

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `single property`() {
        ConfigTypes.values().forEach { type ->
            val values = type.type.configs.forEach {
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

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `all properties`() {
        val objConfig = Configuration()
        val dslConfig = Configuration()
        ConfigTypes.values().forEach { type ->
            val values = type.type.configs.forEach {
                val x = type.type.generator.invoke()
                println("===> ${(it as Enum<*>).name} - $x")
                (it.dslSetter as Configuration.(Any?)-> Unit).invoke(dslConfig, x)
                (it.objSetter as Configuration.(Any?)-> Unit).invoke(objConfig, x)
            }
        }
        assertEquals(objConfig, dslConfig)
    }

}
