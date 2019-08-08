@file:Suppress("TooManyFunctions")

package dev.bombinating.gradle.jooq

import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Generate
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.MatcherRule
import org.jooq.meta.jaxb.Matchers
import org.jooq.meta.jaxb.MatchersTableType
import org.jooq.meta.jaxb.Strategy
import org.jooq.meta.jaxb.Target

fun Configuration.jdbc(action: Jdbc.() -> Unit) {
    jdbc = (jdbc ?: Jdbc()).apply(action)
}

fun Configuration.generator(action: Generator.() -> Unit) {
    generator = (generator ?: Generator()).apply(action)
}

fun Generator.database(action: Database.() -> Unit) {
    database = ((database ?: Database()).apply(action))
}

fun Generator.target(action: Target.() -> Unit) {
    target = ((target ?: Target()).apply(action))
}

fun Generator.strategy(action: Strategy.() -> Unit) {
    strategy = ((strategy ?: Strategy()).apply(action))
}

fun Strategy.matchers(action: Matchers.() -> Unit) {
    matchers = ((matchers ?: Matchers()).apply(action))
}

fun Matchers.tables(action: MutableList<MatchersTableType>.() -> Unit) {
    tables = ((tables ?: mutableListOf()).apply(action))
}

fun MutableList<MatchersTableType>.table(action: MatchersTableType.() -> Unit) {
    this += MatchersTableType().apply(action)
}

fun MatchersTableType.pojoClass(action: MatcherRule.() -> Unit) {
    pojoClass = ((pojoClass ?: MatcherRule()).apply(action))
}

fun Generator.generate(action: Generate.() -> Unit) {
    generate = ((generate ?: Generate()).apply(action))
}

fun Database.forcedTypes(action: MutableList<ForcedType>.() -> Unit) {
    forcedTypes = ((forcedTypes ?: mutableListOf()).apply(action))
}

fun MutableList<ForcedType>.forcedType(action: ForcedType.() -> Unit) {
    this += ForcedType().apply(action)
}
