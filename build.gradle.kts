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
