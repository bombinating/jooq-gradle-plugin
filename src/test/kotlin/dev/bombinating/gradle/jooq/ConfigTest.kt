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
import org.gradle.api.Action
import org.gradle.api.file.FileCollection
import org.gradle.process.BaseExecSpec
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.process.ExecResult
import org.gradle.process.JavaDebugOptions
import org.gradle.process.JavaExecSpec
import org.gradle.process.JavaForkOptions
import org.gradle.process.ProcessForkOptions
import org.gradle.process.internal.JavaExecAction
import org.jooq.meta.jaxb.CatalogMappingType
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Embeddable
import org.jooq.meta.jaxb.EmbeddableField
import org.jooq.meta.jaxb.EnumType
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Generate
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Matchers
import org.jooq.meta.jaxb.MatchersTableType
import org.jooq.meta.jaxb.Property
import org.jooq.meta.jaxb.SchemaMappingType
import org.jooq.meta.jaxb.Strategy
import org.jooq.meta.jaxb.Target
import org.junit.jupiter.api.Test
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextInt
import kotlin.test.assertEquals
import org.jooq.meta.jaxb.Nullability
import org.jooq.meta.jaxb.ForcedTypeObjectType
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.reflect.KClass
import org.jooq.meta.jaxb.MatcherRule
import org.jooq.meta.jaxb.MatcherTransformType

internal val Configuration.xJdbc
    get() = withJdbc(jdbc ?: Jdbc()).jdbc
internal val Configuration.xGenerator
    get() = withGenerator(generator ?: Generator()).generator
internal val Generator.xGenerate
    get() = withGenerate(generate ?: Generate()).generate
internal val Generator.xDatabase
    get() = withDatabase(database ?: Database()).database
internal val Generator.xTarget
    get() = withTarget(target ?: Target()).target
internal val Generator.xStrategy
    get() = withStrategy(strategy ?: Strategy()).strategy
internal val Strategy.xMatcher
    get() = withMatchers(matchers ?: Matchers()).matchers
internal val Matchers.xTables
    get() = withTables(tables ?: mutableListOf<MatchersTableType>()).tables
internal val Database.xEnumTypes
    get() = withEnumTypes(enumTypes ?: mutableListOf()).enumTypes
internal val Database.xForcedTypes
    get() = withForcedTypes(forcedTypes ?: mutableListOf<ForcedType>()).forcedTypes
internal val Database.xCatalogs
    get() = withCatalogs(catalogs ?: mutableListOf()).catalogs
internal val Database.xEmbeddables
    get() = withEmbeddables(embeddables ?: mutableListOf()).embeddables

internal interface ConfigInfo<in T> {
    val objSetter: Configuration.(T) -> Unit
    val dslSetter: JooqConfig.(T) -> Unit
}

internal data class ConfigInfoGen<T>(val configs: List<ConfigInfo<T>>, val generator: () -> T)

internal enum class StringConfigInfo(
    override val objSetter: Configuration.(String) -> Unit,
    override val dslSetter: JooqConfig.(String) -> Unit
) : ConfigInfo<String> {

    // JDBC tests
    JdbcDriver(
        { xJdbc.driver = it },
        { jdbc { driver = it } }
    ),
    JdbcUsername(
        { xJdbc.username = it },
        { jdbc { username = it } }
    ),
    JdbcPassword(
        { xJdbc.password = it },
        { jdbc { password = it } }
    ),
    JdbcUrl(
        { xJdbc.url = it },
        { jdbc { url = it } }
    ),
    JdbcUser(
        { xJdbc.user = it },
        { jdbc { user = it } }
    ),
    JdbcSchema(
        { xJdbc.schema = it },
        { jdbc { schema = it } }
    ),

    // Database tests
    DbName(
        { xGenerator.xDatabase.name = it },
        { generator { database { name = it } } }
    ),
    StrategyName(
        { xGenerator.xStrategy.name = it },
        { generator { strategy { name = it } } }
    ),

    TablePojoClassExpression(
        { xGenerator.xStrategy.xMatcher.xTables +=  MatchersTableType().apply { pojoClass = MatcherRule().apply { expression = it } } },
        { generator { strategy { matchers { tables { table { pojoClass { expression = it }  }  } } } } }
    ),

    DbIncludes(
        { xGenerator.xDatabase.includes = it },
        { generator { database { includes = it } } }
    ),
    DbExcludes(
        { xGenerator.xDatabase.excludes = it },
        { generator { database { excludes = it } } }
    ),
    DbInputCatalog(
        { xGenerator.xDatabase.inputCatalog = it },
        { generator { database { inputCatalog = it } } }
    ),
    DbInputSchema(
        { xGenerator.xDatabase.inputSchema = it },
        { generator { database { inputSchema = it } } }
    ),

    // MatchersTableType tests
    TableDaoImplements(
        { xGenerator.xStrategy.xMatcher.xTables += MatchersTableType().apply { daoImplements = it } },
        { generator { strategy { matchers { tables { table { daoImplements = it } } } } } }
    ),
    TableExpression(
        { xGenerator.xStrategy.xMatcher.xTables += MatchersTableType().apply { expression = it } },
        { generator { strategy { matchers { tables { table { expression = it } } } } } }
    ),
    TableInterfaceImplements(
        { xGenerator.xStrategy.xMatcher.xTables += MatchersTableType().apply { interfaceImplements = it } },
        { generator { strategy { matchers { tables { table { interfaceImplements = it } } } } } }
    ),
    TablePojoExtends(
        { xGenerator.xStrategy.xMatcher.xTables += MatchersTableType().apply { pojoExtends = it } },
        { generator { strategy { matchers { tables { table { pojoExtends = it } } } } } }
    ),
    TablePojoImplements(
        { xGenerator.xStrategy.xMatcher.xTables += MatchersTableType().apply { pojoImplements = it } },
        { generator { strategy { matchers { tables { table { pojoImplements = it } } } } } }
    ),

    // Target tests
    TargetDirectory(
        { xGenerator.xTarget.directory = it },
        { generator { target { directory = it } } }
    ),
    TargetEncoding(
        { xGenerator.xTarget.encoding = it },
        { generator { target { encoding = it } } }
    ),

    TargetPackageName(
        { xGenerator.xTarget.packageName = it },
        { generator { target { packageName = it } } }
    ),

    // CataLogMappingType
    InputCatalog(
        { xGenerator.xDatabase.xCatalogs += CatalogMappingType().apply { inputCatalog = it } },
        { generator { database { catalogs { catalogMappingType { inputCatalog = it } } } } }
    ),
    OutputCatalog(
        { xGenerator.xDatabase.xCatalogs += CatalogMappingType().apply { outputCatalog = it } },
        { generator { database { catalogs { catalogMappingType { outputCatalog = it } } } } }
    ),

    // SchemaMappingType
    InputSchema(
        {
            xGenerator.xDatabase.xCatalogs += CatalogMappingType().apply {
                schemata = mutableListOf(SchemaMappingType().apply { inputSchema = it })
            }
        },
        {
            generator {
                database {
                    catalogs {
                        catalogMappingType {
                            schemata {
                                schemaMappingType {
                                    inputSchema = it
                                }
                            }
                        }
                    }
                }
            }
        }
    ),
    OutputSchema(
        {
            xGenerator.xDatabase.xCatalogs += CatalogMappingType().apply {
                schemata = mutableListOf(SchemaMappingType().apply { outputSchema = it })
            }
        },
        {
            generator {
                database {
                    catalogs {
                        catalogMappingType {
                            schemata {
                                schemaMappingType {
                                    outputSchema = it
                                }
                            }
                        }
                    }
                }
            }
        }
    ),

    // Embeddable
    EmbeddableName(
        { xGenerator.xDatabase.xEmbeddables += Embeddable().apply { name = it } },
        { generator { database { embeddables { embeddable { name = it } } } } }
    ),
    EmbeddableFieldName(
        {
            xGenerator.xDatabase.xEmbeddables += Embeddable().apply {
                fields = mutableListOf(EmbeddableField().apply { name = it })
            }
        },
        { generator { database { embeddables { embeddable { field { name = it } } } } } }
    ),

    EmbeddableFieldExpression(
        {
            xGenerator.xDatabase.xEmbeddables += Embeddable().apply {
                fields = mutableListOf(EmbeddableField().apply { expression = it })
            }
        },
        { generator { database { embeddables { embeddable { field { expression = it } } } } } }
    ),

    // ForcedTypes
    ForcedTypeBinding(
        { xGenerator.xDatabase.xForcedTypes += ForcedType().apply { binding = it } },
        { generator { database { forcedTypes { forcedType { binding = it } } } } }
    ),
    ForcedTypeConverter(
        { xGenerator.xDatabase.xForcedTypes += ForcedType().apply { converter = it } },
        { generator { database { forcedTypes { forcedType { converter = it } } } } }
    ),
    ForcedTypeExcludeExpression(
        { xGenerator.xDatabase.xForcedTypes += ForcedType().apply { excludeExpression = it } },
        { generator { database { forcedTypes { forcedType { excludeExpression = it } } } } }
    ),
    ForcedTypeExcludeTypes(
        { xGenerator.xDatabase.xForcedTypes += ForcedType().apply { excludeTypes = it } },
        { generator { database { forcedTypes { forcedType { excludeTypes = it } } } } }
    ),
    ForcedTypeExpression(
        { xGenerator.xDatabase.xForcedTypes += ForcedType().apply { expression = it } },
        { generator { database { forcedTypes { forcedType { expression = it } } } } }
    ),
    ForcedTypeExpressions(
        { xGenerator.xDatabase.xForcedTypes += ForcedType().apply { expressions = it } },
        { generator { database { forcedTypes { forcedType { expressions = it } } } } }
    ),
    ForcedTypeIncludeExpression(
        { xGenerator.xDatabase.xForcedTypes += ForcedType().apply { includeExpression = it } },
        { generator { database { forcedTypes { forcedType { includeExpression = it } } } } }
    ),
    ForcedTypeIncludeTypes(
        { xGenerator.xDatabase.xForcedTypes += ForcedType().apply { includeTypes = it } },
        { generator { database { forcedTypes { forcedType { includeTypes = it } } } } }
    ),
    ForcedTypeSql(
        { xGenerator.xDatabase.xForcedTypes += ForcedType().apply { sql = it } },
        { generator { database { forcedTypes { forcedType { sql = it } } } } }
    ),
    ForcedTypeName(
        { xGenerator.xDatabase.xForcedTypes += ForcedType().apply { name = it } },
        { generator { database { forcedTypes { forcedType { name = it } } } } }
    ),
    ForcedTypeTypes(
        { xGenerator.xDatabase.xForcedTypes += ForcedType().apply { types = it } },
        { generator { database { forcedTypes { forcedType { types = it } } } } }
    ),
    ForcedTypeUserType(
        { xGenerator.xDatabase.xForcedTypes += ForcedType().apply { userType = it } },
        { generator { database { forcedTypes { forcedType { userType = it } } } } }
    ),
}

internal enum class BooleanConfigInfo(
    override val objSetter: Configuration.(Boolean) -> Unit,
    override val dslSetter: JooqConfig.(Boolean) -> Unit
) : ConfigInfo<Boolean> {

    // Database tests
    DbIncludeRoutines(
        { xGenerator.xDatabase.isIncludeRoutines = it },
        { generator { database { isIncludeRoutines = it } } }
    ),
    DbDateAsTimestamp(
        { xGenerator.xDatabase.isDateAsTimestamp = it },
        { generator { database { isDateAsTimestamp = it } } }
    ),
    DbForceIntegerTypesOnZeroScaleDecimals(
        { xGenerator.xDatabase.isForceIntegerTypesOnZeroScaleDecimals = it },
        { generator { database { isForceIntegerTypesOnZeroScaleDecimals = it } } }
    ),
    DbIgnoreProcedureReturnValues(
        { xGenerator.xDatabase.isIgnoreProcedureReturnValues = it },
        { generator { database { isIgnoreProcedureReturnValues = it } } }
    ),
    DbIncludeExcludeColumns(
        { xGenerator.xDatabase.isIncludeExcludeColumns = it },
        { generator { database { isIncludeExcludeColumns = it } } }
    ),
    DbIncludeForeignKeys(
        { xGenerator.xDatabase.isIncludeForeignKeys = it },
        { generator { database { isIncludeForeignKeys = it } } }
    ),
    DbIncludeIndexes(
        { xGenerator.xDatabase.isIncludeIndexes = it },
        { generator { database { isIncludeIndexes = it } } }
    ),
    DBIncludePackageConstants(
        { xGenerator.xDatabase.isIncludePackageConstants = it },
        { generator { database { isIncludePackageConstants = it } } }
    ),
    DbIncludePackageRoutines(
        { xGenerator.xDatabase.isIncludePackageRoutines = it },
        { generator { database { isIncludePackageRoutines = it } } }
    ),
    DbIncludePackageUDTs(
        { xGenerator.xDatabase.isIncludePackageUDTs = it },
        { generator { database { isIncludePackageUDTs = it } } }
    ),
    DbIncludePrimaryKeys(
        { xGenerator.xDatabase.isIncludePrimaryKeys = it },
        { generator { database { isIncludePrimaryKeys = it } } }
    ),
    DbIncludeSequences(
        { xGenerator.xDatabase.isIncludeSequences = it },
        { generator { database { isIncludeSequences = it } } }
    ),
    DbIncludeTriggerRoutines(
        { xGenerator.xDatabase.isIncludeTriggerRoutines = it },
        { generator { database { isIncludeTriggerRoutines = it } } }
    ),
    DbIncludeUniqueKeys(
        { xGenerator.xDatabase.isIncludeUniqueKeys = it },
        { generator { database { isIncludeUniqueKeys = it } } }
    ),

    // Target
    TargetIsClean(
        { xGenerator.xTarget.isClean = it },
        { generator { target { isClean = it } } }
    ),

    // XCatalogs or database

    IsOutputCatalogToDefault(
        { xGenerator.xDatabase.xCatalogs += CatalogMappingType().apply { isOutputCatalogToDefault = it } },
        { generator { database { catalogs { catalogMappingType { isOutputCatalogToDefault = it } } } } }
    ),
    IsOutputSchemaToDefault(
        {
            xGenerator.xDatabase.xCatalogs += CatalogMappingType().apply {
                schemata = mutableListOf(SchemaMappingType().apply { isOutputSchemaToDefault = it })
            }
        },
        {
            generator {
                database {
                    catalogs {
                        catalogMappingType {
                            schemata {
                                schemaMappingType {
                                    isOutputSchemaToDefault = it
                                }
                            }
                        }
                    }
                }
            }
        }
    ),

    // ForcedType
    ForcedTypeIsEnumConverter(
        { xGenerator.xDatabase.xForcedTypes += ForcedType().apply { isEnumConverter = it } },
        { generator { database { forcedTypes { forcedType { isEnumConverter = it } } } } }
    ),

    // Generate
    GenerateIsComments(
        { xGenerator.xGenerate.isComments = it },
        { generator { generate { isComments = it } } }
    ),
    GenerateIsCommentsOnAttributes(
        { xGenerator.xGenerate.isCommentsOnAttributes = it },
        { generator { generate { isCommentsOnAttributes = it } } }
    ),
    GenerateIsCommentsOnCatalogs(
        { xGenerator.xGenerate.isCommentsOnCatalogs = it },
        { generator { generate { isCommentsOnCatalogs = it } } }
    ),
    GenerateIsCommentsOnKeys(
        { xGenerator.xGenerate.isCommentsOnKeys = it },
        { generator { generate { isCommentsOnKeys = it } } }
    ),
    GenerateIsCommentsOnColumns(
        { xGenerator.xGenerate.isCommentsOnColumns = it },
        { generator { generate { isCommentsOnColumns = it } } }
    ),
    GenerateIsCommentsOnLinks(
        { xGenerator.xGenerate.isCommentsOnLinks = it },
        { generator { generate { isCommentsOnLinks = it } } }
    ),
    GenerateIsCommentsOnPackages(
        { xGenerator.xGenerate.isCommentsOnPackages = it },
        { generator { generate { isCommentsOnPackages = it } } }
    ),
    GenerateIsCommentsOnParameters(
        { xGenerator.xGenerate.isCommentsOnParameters = it },
        { generator { generate { isCommentsOnParameters = it } } }
    ),
    GenerateIsCommentsOnQueues(
        { xGenerator.xGenerate.isCommentsOnQueues = it },
        { generator { generate { isCommentsOnQueues = it } } }
    ),
    GenerateIsCommentsOnRoutines(
        { xGenerator.xGenerate.isCommentsOnRoutines = it },
        { generator { generate { isCommentsOnRoutines = it } } }
    ),
    GenerateIsCommentsOnSchemas(
        { xGenerator.xGenerate.isCommentsOnSchemas = it },
        { generator { generate { isCommentsOnSchemas = it } } }
    ),
    GenerateIsCommentsOnTables(
        { xGenerator.xGenerate.isCommentsOnTables = it },
        { generator { generate { isCommentsOnTables = it } } }
    ),
    GenerateIsCommentsOnUDTs(
        { xGenerator.xGenerate.isCommentsOnUDTs = it },
        { generator { generate { isCommentsOnUDTs = it } } }
    ),
    GenerateIsDaos(
        { xGenerator.xGenerate.isDaos = it },
        { generator { generate { isDaos = it } } }
    ),
    GenerateIsDeprecated(
        { xGenerator.xGenerate.isDeprecated = it },
        { generator { generate { isDeprecated = it } } }
    ),
    GenerateIsDeprecationOnUnknownTypes(
        { xGenerator.xGenerate.isDeprecationOnUnknownTypes = it },
        { generator { generate { isDeprecationOnUnknownTypes = it } } }
    ),
    GeneratedIsEmbeddables(
        { xGenerator.xGenerate.isEmbeddables = it },
        { generator { generate { isEmbeddables = it } } }
    ),
    GeneratedIsEmptyCatalogs(
        { xGenerator.xGenerate.isEmptyCatalogs = it },
        { generator { generate { isEmptyCatalogs = it } } }
    ),
    GenerateIsFluentSetters(
        { xGenerator.xGenerate.isFluentSetters = it },
        { generator { generate { isFluentSetters = it } } }
    ),
    GenerateIsGeneratedAnnotation(
        { xGenerator.xGenerate.isGeneratedAnnotation = it },
        { generator { generate { isGeneratedAnnotation = it } } }
    ),
    GenerateIsGlobalCatalogReferences(
        { xGenerator.xGenerate.isGlobalCatalogReferences = it },
        { generator { generate { isGlobalCatalogReferences = it } } }
    ),
    GenerateIsGlobalKeyReferences(
        { xGenerator.xGenerate.isGlobalKeyReferences = it },
        { generator { generate { isGlobalKeyReferences = it } } }
    ),
    GenerateIsGlobalLinkReferences(
        { xGenerator.xGenerate.isGlobalLinkReferences = it },
        { generator { generate { isGlobalLinkReferences = it } } }
    ),
    GenerateIsGlobalQueueReferences(
        { xGenerator.xGenerate.isGlobalQueueReferences = it },
        { generator { generate { isGlobalQueueReferences = it } } }
    ),
    GenerateIsGlobalRoutineReferences(
        { xGenerator.xGenerate.isGlobalRoutineReferences = it },
        { generator { generate { isGlobalRoutineReferences = it } } }
    ),
    GenerateIsGlobalSchemaReferences(
        { xGenerator.xGenerate.isGlobalSchemaReferences = it },
        { generator { generate { isGlobalSchemaReferences = it } } }
    ),
    GenerateIsGlobalSequenceReferences(
        { xGenerator.xGenerate.isGlobalSequenceReferences = it },
        { generator { generate { isGlobalSequenceReferences = it } } }
    ),
    GenerateIsGlobalTableReferences(
        { xGenerator.xGenerate.isGlobalTableReferences = it },
        { generator { generate { isGlobalTableReferences = it } } }
    ),
    GenerateIsGlobalUDTReferences(
        { xGenerator.xGenerate.isGlobalUDTReferences = it },
        { generator { generate { isGlobalUDTReferences = it } } }
    ),
    // FIXME: and more!
}

private fun <T : Enum<*>> Int.toEnum(clazz: KClass<T>): T =
    clazz.java.enumConstants[(if (this < 0) (this * -1) else this) % clazz.java.enumConstants.size]

internal enum class IntConfigInfo(
    override val objSetter: Configuration.(Int) -> Unit,
    override val dslSetter: JooqConfig.(Int) -> Unit
) : ConfigInfo<Int> {
    LogSlowerQueriesAfterSecs(
        { xGenerator.xDatabase.logSlowQueriesAfterSeconds = it },
        { generator { database { logSlowQueriesAfterSeconds = it } } }),
    // FIXME: where should this go?!
    ForcedTypeNullability(
        { xGenerator.xDatabase.xForcedTypes += ForcedType().apply { nullability = it.toEnum(Nullability::class) } },
        { generator { database { forcedTypes { forcedType { nullability = it.toEnum(Nullability::class) } } } } }
    ),
    ForcedTypeForcedTypeObjectType(
        {
            xGenerator.xDatabase.xForcedTypes += ForcedType().apply {
                objectType = it.toEnum(ForcedTypeObjectType::class)
            }
        },
        {
            generator {
                database {
                    forcedTypes {
                        forcedType {
                            objectType = it.toEnum(ForcedTypeObjectType::class)
                        }
                    }
                }
            }
        }
    ),

    TablePojoClassTransform(
        { xGenerator.xStrategy.xMatcher.xTables +=  MatchersTableType().apply { pojoClass = MatcherRule().apply { transform = it.toEnum(MatcherTransformType::class) } } },
        { generator { strategy { matchers { tables { table { pojoClass { transform = it.toEnum(MatcherTransformType::class) }  }  } } } } }
    ),

}

internal enum class StringPairConfigInfo(
    override val objSetter: Configuration.(Pair<String, String>) -> Unit,
    override val dslSetter: JooqConfig.(Pair<String, String>) -> Unit
) : ConfigInfo<Pair<String, String>> {
    // Database
    DatabasePropertiesExplicit(
        { xGenerator.xDatabase.properties = mutableListOf(Property().apply { key = it.first; value = it.second }) },
        { generator { database { properties { property(it.first to it.second) } } } }
    ),
    DatabasePropertiesImplict(
        { xGenerator.xDatabase.properties = mutableListOf(Property().apply { key = it.first; value = it.second }) },
        { generator { database { properties(it.first to it.second) } } }
    ),

    // JDBC
    JdbcPropertiesExplicit(
        { xJdbc.properties = mutableListOf(Property().apply { key = it.first; value = it.second }) },
        { jdbc { properties { property(it.first to it.second) } } }
    ),
    JdbcPropertiesImplict(
        { xJdbc.properties = mutableListOf(Property().apply { key = it.first; value = it.second }) },
        { jdbc { properties(it.first to it.second) } }
    ),

    // Database or EnumType
    EnumTypesExplicit(
        { xGenerator.xDatabase.enumTypes = mutableListOf(EnumType().apply { name = it.first; literals = it.second }) },
        { generator { database { enumTypes { enumType { name = it.first; literals = it.second } } } } }
    ),
    EnumTypesImplicit(
        { xGenerator.xDatabase.enumTypes = mutableListOf(EnumType().apply { name = it.first; literals = it.second }) },
        { generator { database { enumTypes(it.first to it.second) } } }
    ),

    // Database or Embeddable
    EmbeddableFields(
        {
            xGenerator.xDatabase.xEmbeddables += Embeddable().apply {
                fields = mutableListOf(EmbeddableField().apply { name = it.first; expression = it.second })
            }
        },
        { generator { database { embeddables { embeddable { fields(it.first to it.second) } } } } })
}

internal enum class ConfigTypes(val type: ConfigInfoGen<*>) {
    STRING(ConfigInfoGen(StringConfigInfo.values().toList()) { randomAlphanumeric(1, 20) }),
    BOOLEAN(ConfigInfoGen(BooleanConfigInfo.values().toList()) { nextBoolean() }),
    INT(ConfigInfoGen(IntConfigInfo.values().toList()) { nextInt() }),
    STRING_PROPS(ConfigInfoGen(StringPairConfigInfo.values().toList()) {
        randomAlphanumeric(1, 20) to randomAlphanumeric(1, 20)
    })
}

class ConfigTest {

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `single property`() {
        ConfigTypes.values().forEach { type ->
            type.type.configs.forEach {
                val objConfig = Configuration()
                val dslConfig = JooqConfigImpl()
                val randomValue = type.type.generator.invoke()
                (it.dslSetter as JooqConfig.(Any?) -> Unit).invoke(dslConfig, randomValue)
                (it.objSetter as Configuration.(Any?) -> Unit).invoke(objConfig, randomValue)
                assertEquals(objConfig, dslConfig.config)
            }

        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `all properties`() {
        val objConfig = Configuration()
        val dslConfig = JooqConfigImpl()
        ConfigTypes.values().forEach { type ->
            type.type.configs.forEach {
                val randomValue = type.type.generator.invoke()
                (it.dslSetter as JooqConfig.(Any?) -> Unit).invoke(dslConfig, randomValue)
                (it.objSetter as Configuration.(Any?) -> Unit).invoke(objConfig, randomValue)
            }

        }
        assertEquals(objConfig, dslConfig.config)
    }

    @Test
    fun `String Pair to Property Conversion`() {
        val key = "abc"
        val value = "xyz"
        val expected = Property().apply {
            this.key = key
            this.value = value
        }
        val computed = (key to value).toProperty()
        assertEquals(expected, computed)
    }

    @Test
    fun `String Pair to Enum Type Conversion`() {
        val key = "abc"
        val value = "xyz"
        val expected = EnumType().apply {
            name = key
            literals = value
        }
        val computed = (key to value).toEnumType()
        assertEquals(expected, computed)
    }

    @Test
    fun `String Pair to Embeddable Field Type Conversion`() {
        val key = "abc"
        val value = "xyz"
        val expected = EmbeddableField().apply {
            name = key
            expression = value
        }
        val computed = (key to value).toEmbeddableField()
        assertEquals(expected, computed)
    }

//    @Test
//    fun `Result Handler`() {
//        val exitValue = 1
//        var y: Int? = null
//        val x = JooqConfigImpl().apply {
//            resultHandler {
//                y = exitValue
//            }
//        }
//        x.resultHandler?.invoke(object : ExecResult {
//            override fun getExitValue(): Int = exitValue
//            override fun assertNormalExitValue(): ExecResult = this
//            override fun rethrowFailure(): ExecResult = this
//        })
//        assertEquals(exitValue, y)
//    }

    @Test
    fun `Run Config`() {
        val executable = "abc"
        var y: String? = null
        val x = JooqConfigImpl().apply {
            runConfig {
                y = executable
            }
        }
        x.runConfig?.invoke(object : JavaExecAction {
            override fun setSystemProperties(properties: MutableMap<String, *>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getExecutable(): String = executable

            override fun setDefaultCharacterEncoding(defaultCharacterEncoding: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun jvmArgs(arguments: MutableIterable<*>?): JavaForkOptions {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun jvmArgs(vararg arguments: Any?): JavaForkOptions {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun environment(environmentVariables: MutableMap<String, *>?): ProcessForkOptions {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun environment(name: String?, value: Any?): ProcessForkOptions {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setMinHeapSize(heapSize: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getCommandLine(): MutableList<String> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun systemProperties(properties: MutableMap<String, *>?): JavaForkOptions {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setExecutable(executable: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setExecutable(executable: Any?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun executable(executable: Any?): ProcessForkOptions {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun debugOptions(action: Action<JavaDebugOptions>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun classpath(vararg paths: Any?): JavaExecSpec {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setJvmArgs(arguments: MutableList<String>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setJvmArgs(arguments: MutableIterable<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setAllJvmArgs(arguments: MutableList<String>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setAllJvmArgs(arguments: MutableIterable<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setDebug(enabled: Boolean) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getStandardOutput(): OutputStream {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getClasspath(): FileCollection {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getArgs(): MutableList<String> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getWorkingDir(): File {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setBootstrapClasspath(classpath: FileCollection?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getDebugOptions(): JavaDebugOptions {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getDefaultCharacterEncoding(): String? {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setMaxHeapSize(heapSize: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setArgs(args: MutableList<String>?): JavaExecSpec {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setArgs(args: MutableIterable<*>?): JavaExecSpec {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getErrorOutput(): OutputStream {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun systemProperty(name: String?, value: Any?): JavaForkOptions {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setStandardOutput(outputStream: OutputStream?): BaseExecSpec {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getBootstrapClasspath(): FileCollection {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun workingDir(dir: Any?): ProcessForkOptions {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setWorkingDir(dir: File?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setWorkingDir(dir: Any?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setEnvironment(environmentVariables: MutableMap<String, *>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun args(vararg args: Any?): JavaExecSpec {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun args(args: MutableIterable<*>?): JavaExecSpec {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getEnableAssertions(): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getArgumentProviders(): MutableList<CommandLineArgumentProvider> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setEnableAssertions(enabled: Boolean) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getMaxHeapSize(): String? {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setIgnoreExitValue(ignoreExitValue: Boolean): BaseExecSpec {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getStandardInput(): InputStream {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setStandardInput(inputStream: InputStream?): BaseExecSpec {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setErrorOutput(outputStream: OutputStream?): BaseExecSpec {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun copyTo(options: JavaForkOptions?): JavaForkOptions {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun copyTo(options: ProcessForkOptions?): ProcessForkOptions {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun isIgnoreExitValue(): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getJvmArgs(): MutableList<String> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getJvmArgumentProviders(): MutableList<CommandLineArgumentProvider> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun execute(): ExecResult {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getSystemProperties(): MutableMap<String, Any> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getMinHeapSize(): String? {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getEnvironment(): MutableMap<String, Any> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getAllJvmArgs(): MutableList<String> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getDebug(): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getMain(): String? {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setClasspath(classpath: FileCollection?): JavaExecSpec {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun setMain(main: String?): JavaExecSpec {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun bootstrapClasspath(vararg classpath: Any?): JavaForkOptions {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
        assertEquals(executable, y)
    }

}
