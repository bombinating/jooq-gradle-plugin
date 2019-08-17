
import com.jfrog.bintray.gradle.BintrayExtension
import groovy.lang.GroovyObject
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig

fun Project.prop(s: String) = findProperty(s) as String?
val pubName = "jooqPlugin"
val kdocLoc = "$buildDir/kdoc"
val bintrayUser = prop("bintrayUser")
val bintrayKey = prop("bintrayKey")
val gitUrl = "https://github.com/bombinating/jooq-gradle-plugin.git"

plugins {
    kotlin("jvm") version "1.3.41"
    `java-gradle-plugin`
    `maven-publish`
    id("net.researchgate.release") version "2.8.1"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC16"
    id("org.jetbrains.dokka") version "0.9.18"
    id("com.jfrog.bintray") version "1.8.4"
    id("com.jfrog.artifactory") version "4.9.8"
    id("com.gradle.plugin-publish") version "0.10.1"
}

group = "dev.bombinating"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    api(gradleApi())
    api(kotlin("stdlib-jdk8"))
    api(group = "org.jooq", name = "jooq-codegen", version = "3.11.11")
    api(group = "javax.xml.bind", name = "jaxb-api", version = "2.3.1")
    api(group = "javax.activation", name = "activation", version = "1.1.1")
    api(group = "com.sun.xml.bind", name = "jaxb-core", version = "2.3.0.1")
    testImplementation(gradleTestKit())
    testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-test-junit5", version = "1.3.41")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-params", version = "5.5.1")
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.5.1")
    testImplementation(group = "org.testcontainers", name = "junit-jupiter", version = "1.11.3")
    testImplementation(group = "org.testcontainers", name = "postgresql", version = "1.11.3")
    testImplementation(group = "org.testcontainers", name = "mssqlserver", version = "1.11.3")
    testImplementation(group = "org.postgresql", name = "postgresql", version = "42.2.6")
    testImplementation(group = "com.microsoft.sqlserver", name = "mssql-jdbc", version = "7.4.1.jre8")
    testImplementation(group = "com.sun.istack", name = "istack-commons-runtime", version = "3.0.8")
    testImplementation(group = "io.github.microutils", name = "kotlin-logging", version = "1.7.3")
    testImplementation(group = "org.apache.commons", name = "commons-lang3", version = "3.9")
    testImplementation(group = "com.h2database", name = "h2", version = "1.4.199")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

gradlePlugin {
    plugins {
        create("jooqPlugin") {
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
        register(pubName, MavenPublication::class) {
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
