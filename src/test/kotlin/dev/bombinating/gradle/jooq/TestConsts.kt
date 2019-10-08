package dev.bombinating.gradle.jooq

const val GEN_DIR = "generated/src/main/java"
const val JOOQ_VERSION = "3.12.1"
const val PACKAGE_NAME = "com.acme.domain.generated"

const val settingsFilename = "settings.gradle.kts"
const val defaultProjectName = "acme-domain"

const val h2JdbcDriverDependency = """group = "com.h2database", name = "h2", version = "1.4.199""""
const val pgJdbcDriverDependency = """group = "org.postgresql", name = "postgresql", version = "42.2.6""""
const val sqlServerJdbcDriverDependency = """group = "com.microsoft.sqlserver", name = "mssql-jdbc", version = "7.4.1.jre8""""

const val jooqOsGroup = "org.jooq"
const val jooqVersion12 = "3.12.1"
const val jooqVersion11 = "3.11.11"

const val defaultGenDir: String = "generated/src/main/java"
const val defaultJooqTaskName: String = "jooq"

const val h2Driver = "org.h2.Driver"
const val h2Url = "jdbc:h2:~/test_db;AUTO_SERVER=true"
const val h2Username = "sa"
const val h2Password = ""

const val defaultSchemaName = "test"
const val defaultTableName = "Person"
const val defaultPackageName = "com.acme.domain.generated"