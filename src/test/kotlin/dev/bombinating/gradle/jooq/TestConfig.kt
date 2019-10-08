package dev.bombinating.gradle.jooq

data class TestConfig(
    val driver: String,
    val url: String,
    val username: String,
    val password: String,
    val schema: String,
    val genDir: String,
    val javaVersion: String,
    val jooqVersion: String,
    val packageName: String,
    val includes: String? = null
)