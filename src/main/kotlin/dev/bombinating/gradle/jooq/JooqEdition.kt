package dev.bombinating.gradle.jooq

enum class JooqEdition(val groupId: String) {
    OSS("org.jooq"),
    PRO("org.jooq.pro"),
    PRO_JAVA_6("org.jooq.pro-java-6"),
    TRIAL("org.jooq.trial");

    companion object {
        val groupIds = values().map { it.groupId }.toSet()
    }

}
