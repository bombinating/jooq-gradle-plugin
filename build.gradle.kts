
import com.jfrog.bintray.gradle.BintrayExtension
import groovy.lang.GroovyObject
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig

/*
 * Main plugin versions
 */
val jooqVersion: String by project
val kotlinVersion: String by project

/*
 * Java 9+ dependency versions
 */
val jaxbApiVersion: String by project
val activationVersion: String by project
val jaxbCoreVersion: String by project
val jaxbImplVersion: String by project

/*
 * Test dependency versions
 */
val junitVersion: String by project
val testContainersVersion: String by project
val h2Version: String by project
val postgresqlJdbcVersion: String by project
val sqlServerJdbcVersion: String by project
val microutilsVersion: String by project
val commonsLang3Version: String by project

/*
 * Distribution info
 */
val bintrayUser: String? by project
val bintrayKey: String? by project

val pubName = "jooqPlugin"
val kdocLoc = "$buildDir/kdoc"
val gitUrl = "https://github.com/bombinating/jooq-gradle-plugin.git"

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
    id("net.researchgate.release")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.dokka")
    id("com.jfrog.bintray")
    id("com.jfrog.artifactory")
    id("com.gradle.plugin-publish")
    id("com.gradle.build-scan")
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    api(gradleApi())
    api(group = "org.jooq", name = "jooq-codegen", version = jooqVersion)
    /*
     * Java 9+ dependencies
     */
    api(group = "javax.activation", name = "activation", version = activationVersion)
    api(group = "javax.xml.bind", name = "jaxb-api", version = jaxbApiVersion)
    api(group = "com.sun.xml.bind", name = "jaxb-core", version = jaxbCoreVersion)
    api(group = "com.sun.xml.bind", name = "jaxb-impl", version = jaxbImplVersion)

    testImplementation(gradleTestKit())
    /*
     * JUnit
     */
    testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-test-junit5", version = kotlinVersion)
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-params", version = junitVersion)
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = junitVersion)
    testImplementation(group = "org.testcontainers", name = "junit-jupiter", version = testContainersVersion)
    /*
     * PostgreSQL
     */
    testImplementation(group = "org.testcontainers", name = "postgresql", version = testContainersVersion)
    testImplementation(group = "org.postgresql", name = "postgresql", version = postgresqlJdbcVersion)
    /*
     * SQL Server
     */
    testImplementation(group = "org.testcontainers", name = "mssqlserver", version = testContainersVersion)
    testImplementation(group = "com.microsoft.sqlserver", name = "mssql-jdbc", version = sqlServerJdbcVersion)
    /*
     * H2
     */
    testImplementation(group = "com.h2database", name = "h2", version = h2Version)
    /*
     * Test utils
     */
    testImplementation(group = "org.apache.commons", name = "commons-lang3", version = commonsLang3Version)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

gradlePlugin {
    plugins {
        create(pubName) {
            displayName = "jOOQ code gen plugin"
            id = "dev.bombinating.jooq-codegen"
            implementationClass = "dev.bombinating.gradle.jooq.JooqPlugin"
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val dokka by tasks.getting(DokkaTask::class) {
    outputFormat = "html"
    outputDirectory = kdocLoc
    jdkVersion = 8
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    archiveClassifier.set("javadoc")
    from(dokka)
}

pluginBundle {
    description = "jOOQ code generation plugin that works with kts"
    vcsUrl = gitUrl
    website = "https://github.com/bombinating/jooq-gradle-plugin"
    tags = listOf("jOOQ", "database", "kts")
}

publishing {
    publications {
        create<MavenPublication>(pubName) {
            from(components.getByName("java"))
            artifact(sourcesJar)
            artifact(dokkaJar)
        }
    }
}

bintray {
    user = bintrayUser
    key = bintrayKey
    publish = true
    setPublications(pubName)
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = "jooq-gradle-plugin"
        setLicenses("Apache-2.0")
        vcsUrl = gitUrl
        githubRepo = "bombinating/jooq-gradle-plugin"
        githubReleaseNotesFile = "README.adoc"
        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = "$version"
            vcsTag = "$version"
        })
    })
}

artifactory {
    setContextUrl("https://oss.jfrog.org/artifactory")
    publish(delegateClosureOf<PublisherConfig> {
        repository(delegateClosureOf<GroovyObject> {
            setProperty("repoKey", "oss-snapshot-local")
            setProperty("username", bintrayUser)
            setProperty("password", bintrayKey)
            setProperty("maven", true)
        })
        defaults(delegateClosureOf<GroovyObject> {
            invokeMethod("publications", pubName)
            setProperty("publishArtifacts", true)
            setProperty("publishPom", true)
        })
    })
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    publishAlways()
}