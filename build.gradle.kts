import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
    `java-gradle-plugin`
    `maven-publish`
    id("net.researchgate.release") version "2.8.1"
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC16"
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
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

gradlePlugin {
    plugins {
        create("jooqPlugin") {
            id = "dev.bombinating.jooq"
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
