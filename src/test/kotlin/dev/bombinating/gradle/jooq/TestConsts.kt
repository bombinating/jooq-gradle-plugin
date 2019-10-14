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

const val settingsFilename = "settings.gradle.kts"
const val defaultProjectName = "acme-domain"

const val h2JdbcDriverDependency = """group = "com.h2database", name = "h2", version = "1.4.199""""
const val pgJdbcDriverDependency = """group = "org.postgresql", name = "postgresql", version = "42.2.6""""
const val sqlServerJdbcDriverDependency =
    """group = "com.microsoft.sqlserver", name = "mssql-jdbc", version = "7.4.1.jre8""""

const val jooqOsGroup = "org.jooq"
const val jooqVersion12 = "3.12.1"
const val jooqVersion11 = "3.11.12"
const val defaultJooqVersion = jooqVersion12

const val defaultGenDir: String = "generated/src/main/java"
const val defaultJooqTaskName: String = "jooq"

const val h2Driver = "org.h2.Driver"
const val h2Url = "jdbc:h2:~/test_db;AUTO_SERVER=true"
const val h2Username = "sa"
const val h2Password = ""

const val defaultDbName = "acmehr"
const val defaultSchemaName = "test"
const val defaultTableName = "Person"
const val defaultPackageName = "com.acme.domain.generated"

const val envVarContainerTests = "JOOQ_CONTAINER_TESTS"
const val containerEnabledValue = "true"
const val envVarProTests = "JOOQ_PRO_TESTS"
const val proTestsEnabledValue = "true"
const val envVarJooqRepoUrl = "JOOQ_REPO_URL"
const val envVarJooqRepoUsername = "JOOQ_REPO_USERNAME"
const val envVarJooqRepoPassword = "JOOQ_REPO_PASSWORD"

val proTestsEnabled: Boolean
    get() = System.getenv(envVarProTests)?.toBoolean() ?: false