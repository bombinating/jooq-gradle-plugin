/*
 * Copyright 2019 Andrew Geery
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.bombinating.gradle.jooq

import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Target
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
internal val Generator.xTarget
    get() = withTarget(target ?: Target()).target

internal interface ConfigInfo<in T> {
    val objSetter: Configuration.(T) -> Unit
    val dslSetter: JooqConfig.(T) -> Unit
}

internal data class ConfigInfoGen<T>(val configs: List<ConfigInfo<T>>, val generator: () -> T)

internal enum class StringConfigInfo(override val objSetter: Configuration.(String) -> Unit, override val dslSetter: JooqConfig.(String) -> Unit) : ConfigInfo<String> {
    JdbcDriver({ xJdbc.driver = it }, { jdbc { driver = it } }),
    JdbcUsername({ xJdbc.username = it }, { jdbc { username = it } }),
    JdbcPassword({ xJdbc.password = it }, { jdbc { password = it } }),
    JdbcUrl({ xJdbc.url = it }, { jdbc { url = it } }),
    JdbcUser({ xJdbc.user = it }, { jdbc { user = it } }),
    JdbcSchema({ xJdbc.schema = it }, { jdbc { schema = it } }),
    DbName({ xGenerator.xDatabase.name = it }, { generator { database { name = it } } }),
    DbIncludes({ xGenerator.xDatabase.includes = it }, { generator { database { includes = it } } }),
    DbExcludes({ xGenerator.xDatabase.excludes = it }, { generator { database { excludes = it } } }),
    DbInputCatalog({ xGenerator.xDatabase.inputCatalog = it }, { generator { database { inputCatalog = it } } }),
    DbInputSchema({ xGenerator.xDatabase.inputSchema = it }, { generator { database { inputSchema = it } } }),
    TargetDirectory({ xGenerator.xTarget.directory = it }, { generator { target { directory = it } } }),
    TargetEncoding({ xGenerator.xTarget.encoding = it }, { generator { target { encoding = it } } }),
    TargetPackageName({ xGenerator.xTarget.packageName = it }, { generator { target { packageName = it } } })
}

internal enum class BooleanConfigInfo(override val objSetter: Configuration.(Boolean) -> Unit, override val dslSetter: JooqConfig.(Boolean) -> Unit) : ConfigInfo<Boolean> {
    DbIncludeRoutines({ xGenerator.xDatabase.isIncludeRoutines = it }, { generator { database { isIncludeRoutines = it } } }),
    DbDateAsTimestamp({ xGenerator.xDatabase.isDateAsTimestamp = it }, { generator { database { isDateAsTimestamp = it } } }),
    DbForceIntegerTypesOnZeroScaleDecimals({ xGenerator.xDatabase.isForceIntegerTypesOnZeroScaleDecimals = it }, { generator { database { isForceIntegerTypesOnZeroScaleDecimals = it } } }),
    DbIgnoreProcedureReturnValues({ xGenerator.xDatabase.isIgnoreProcedureReturnValues = it }, { generator { database { isIgnoreProcedureReturnValues = it } } }),
    DbIncludeExcludeColumns({ xGenerator.xDatabase.isIncludeExcludeColumns = it }, { generator { database { isIncludeExcludeColumns = it } } }),
    DbIncludeForeignKeys({ xGenerator.xDatabase.isIncludeForeignKeys = it }, { generator { database { isIncludeForeignKeys = it } } }),
    DbIncludeIndexes({ xGenerator.xDatabase.isIncludeIndexes = it }, { generator { database { isIncludeIndexes = it } } }),
    DBIncludePackageConstants({ xGenerator.xDatabase.isIncludePackageConstants = it }, { generator { database { isIncludePackageConstants = it } } }),
    DbIncludePackageRoutines({ xGenerator.xDatabase.isIncludePackageRoutines = it }, { generator { database { isIncludePackageRoutines = it } } }),
    DbIncludePackageUDTs({ xGenerator.xDatabase.isIncludePackageUDTs = it }, { generator { database { isIncludePackageUDTs = it } } }),
    DbIncludePrimaryKeys({ xGenerator.xDatabase.isIncludePrimaryKeys = it }, { generator { database { isIncludePrimaryKeys = it } } }),
    DbIncludeSequences({ xGenerator.xDatabase.isIncludeSequences = it }, { generator { database { isIncludeSequences = it } } }),
    DbIncludeTriggerRoutines({ xGenerator.xDatabase.isIncludeTriggerRoutines = it }, { generator { database { isIncludeTriggerRoutines = it } } }),
    DbIncludeUniqueKeys({ xGenerator.xDatabase.isIncludeUniqueKeys = it }, { generator { database { isIncludeUniqueKeys = it } } }),
    TargetIsClean({ xGenerator.xTarget.isClean = it }, { generator { target { isClean = it } } }),
}

internal enum class IntConfigInfo(override val objSetter: Configuration.(Int) -> Unit, override val dslSetter: JooqConfig.(Int) -> Unit) : ConfigInfo<Int> {
    LogSlowerQueriesAfterSecs({ xGenerator.xDatabase.logSlowQueriesAfterSeconds = it }, { generator { database { logSlowQueriesAfterSeconds = it } } })
}

internal enum class ConfigTypes(val type: ConfigInfoGen<*>) {
    STRING(ConfigInfoGen(StringConfigInfo.values().toList()) { randomAlphanumeric(1, 20) }),
    BOOLEAN(ConfigInfoGen(BooleanConfigInfo.values().toList()) { nextBoolean() }),
    INT(ConfigInfoGen(IntConfigInfo.values().toList()) { nextInt() })
}

class ConfigTest {

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `single property`() {
        ConfigTypes.values().forEach { type ->
            type.type.configs.forEach {
                val objConfig = createDefaultConfig()
                val dslConfig = JooqConfigImpl()
                val randomValue = type.type.generator.invoke()
                (it.dslSetter as JooqConfig.(Any?)-> Unit).invoke(dslConfig, randomValue)
                (it.objSetter as Configuration.(Any?)-> Unit).invoke(objConfig, randomValue)
                assertEquals(objConfig, dslConfig.config)
            }

        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `all properties`() {
        val objConfig = createDefaultConfig()
        val dslConfig = JooqConfigImpl()
        ConfigTypes.values().forEach { type ->
            type.type.configs.forEach {
                val randomValue = type.type.generator.invoke()
                (it.dslSetter as JooqConfig.(Any?)-> Unit).invoke(dslConfig, randomValue)
                (it.objSetter as Configuration.(Any?)-> Unit).invoke(objConfig, randomValue)
            }

        }
        assertEquals(objConfig, dslConfig.config)
    }

}
