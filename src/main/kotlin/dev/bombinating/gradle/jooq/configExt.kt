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
@file:Suppress("TooManyFunctions")

/**
 * Extension methods creating for building jOOQ codegen `Configuration`.
 */

package dev.bombinating.gradle.jooq

import org.gradle.process.JavaExecSpec
import org.jooq.meta.jaxb.CatalogMappingType
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Embeddable
import org.jooq.meta.jaxb.EmbeddableField
import org.jooq.meta.jaxb.EnumType
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Generate
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.MatcherRule
import org.jooq.meta.jaxb.Matchers
import org.jooq.meta.jaxb.MatchersTableType
import org.jooq.meta.jaxb.Property
import org.jooq.meta.jaxb.SchemaMappingType
import org.jooq.meta.jaxb.Strategy
import org.jooq.meta.jaxb.Target

/**
 * Extension method for customizing the JVM environment the jOOQ code generation process runs in.
 *
 * @receiver Parent jOOQ code generation [JooqConfig] the run configuration is associated with
 * @param action lambda for customizing the JVM environment the jOOQ code generation process runs in
 */
fun JooqConfig.runConfig(action: JavaExecSpec.() -> Unit) {
    runConfig = action
}

/**
 * Extension method for specifying a handler to be invoked after the jOOQ code generation completes.
 *
 * @receiver Parent jOOQ code generation [JooqConfig] the result handler is associated with
 * @param action lambda for specifying a handler to be invoked after the jOOQ code generation completes
 */
fun JooqConfig.resultHandler(action: JavaExecResult.() -> Unit) {
    resultHandler = action
}

/**
 * Extension method for customizing the `Jdbc` config in a jOOQ code generation [JooqConfig]
 *
 * @receiver Parent jOOQ code generation [JooqConfig] the `Jdbc` config is associated with
 * @param action lambda for customizing the `Jdbc` config
 */
fun JooqConfig.jdbc(action: Jdbc.() -> Unit) {
    jdbc = (jdbc ?: Jdbc()).apply(action)
}

/**
 * Extension method for customizing the `Generator` config in a jOOQ code generation [JooqConfig]
 *
 * @receiver Parent jOOQ code generation [JooqConfig] the `Generator` config is associated with
 * @param action lambda for customizing the `Generator` config
 */
fun JooqConfig.generator(action: Generator.() -> Unit) {
    generator = (generator ?: Generator()).apply(action)
}

/**
 * Extension method for customizing the `Database` config in a `Generator` config
 *
 * @receiver Parent `Generator` the `Database` config is associated with
 * @param action lambda for customizing the `Database` config
 */
fun Generator.database(action: Database.() -> Unit) {
    database = ((database ?: Database()).apply(action))
}

/**
 * Extension method for customizing the `Target` config in a `Generator` config
 *
 * @receiver Parent `Generator` the `Target` config is associated with
 * @param action lambda for customizing the `Target` config
 */
fun Generator.target(action: Target.() -> Unit) {
    target = ((target ?: Target()).apply(action))
}

/**
 * Extension method for customizing the `Strategy` config in a `Generator` config
 *
 * @receiver Parent `Generator` the `Strategy` config is associated with
 * @param action lambda for customizing the `Strategy` config
 */
fun Generator.strategy(action: Strategy.() -> Unit) {
    strategy = ((strategy ?: Strategy()).apply(action))
}

/**
 * Extension method for customizing the `Matchers` config in a `Strategy` config
 *
 * @receiver Parent `Strategy` the `Matchers` config is associated with
 * @param action lambda for customizing the `Matchers` config
 */
fun Strategy.matchers(action: Matchers.() -> Unit) {
    matchers = ((matchers ?: Matchers()).apply(action))
}

/**
 * Extension method for customizing `tables` config in a `matchers` config
 *
 * @receiver Parent `Matchers` the [MutableList] of `MatchersTableType` config is associated with
 * @param action lambda for customizing the [MutableList] of `MatchersTableType` config
 */
fun Matchers.tables(action: MutableList<MatchersTableType>.() -> Unit) {
    tables = ((tables ?: mutableListOf()).apply(action))
}

/**
 * Extension method for customizing the `table` config in a `tables` config
 *
 * @receiver Parent [MutableList] of `MatchersTableType` the `MatchersTableType` config is associated with
 * @param action lambda for customizing the `MatchersTableType` config
 */
fun MutableList<MatchersTableType>.table(action: MatchersTableType.() -> Unit) {
    this += MatchersTableType().apply(action)
}

/**
 * Extension method for customizing the `pojoClass` config in a `matcher` config
 *
 * @receiver Parent `MatchersTableType` the `MatcherRule` config is associated with
 * @param action lambda for customizing the `MatcherRule` config
 */
fun MatchersTableType.pojoClass(action: MatcherRule.() -> Unit) {
    pojoClass = ((pojoClass ?: MatcherRule()).apply(action))
}

/**
 * Extension method for customizing the `Generate` config in a `Generator` config
 *
 * @receiver Parent `Generator` the `Generate` config is associated with
 * @param action lambda for customizing the `Generate` config
 */
fun Generator.generate(action: Generate.() -> Unit) {
    generate = ((generate ?: Generate()).apply(action))
}

/**
 * Extension method for customizing the `forcedTypes` config in a `Database` config
 *
 * @receiver Parent `Database` the [MutableList] of `ForcedType` config is associated with
 * @param action lambda for customizing the [MutableList] of `ForcedType` config
 */
fun Database.forcedTypes(action: MutableList<ForcedType>.() -> Unit) {
    forcedTypes = ((forcedTypes ?: mutableListOf()).apply(action))
}

/**
 * Extension method for customizing the `forcesType` config in a `forcedTypes` block.
 *
 * @receiver Parent [MutableList] of `ForcedType` the `ForcedType` config is associated with
 * @param action lambda for customizing the `ForcedType` config
 */
fun MutableList<ForcedType>.forcedType(action: ForcedType.() -> Unit) {
    this += ForcedType().apply(action)
}

/**
 * Extension method for customizing the `properties` config in a `Database` config
 *
 * @receiver Parent `Database` the [MutableList] of `Property` config is associated with
 * @param action lambda for customizing the [MutableList] of `Property` config
 */
fun Database.properties(action: MutableList<Property>.() -> Unit) {
    properties = ((properties ?: mutableListOf()).apply(action))
}

/**
 * Extension method for customizing the `property` config in a `properties` block.
 *
 * @receiver Parent [MutableList] of `Property` the `Property` config is associated with
 * @param prop String Pair
 */
fun MutableList<Property>.property(prop: Pair<String, String>) {
    this += prop.toProperty()
}

// FIXME: new below this -- need kdocs!

fun Database.properties(vararg props: Pair<String, String>) {
    properties = props.map(Pair<String, String>::toProperty).toMutableList()
}

fun Jdbc.properties(action: MutableList<Property>.() -> Unit) {
    properties = ((properties ?: mutableListOf()).apply(action))
}

fun Jdbc.properties(vararg props: Pair<String, String>) {
    properties = props.map(Pair<String, String>::toProperty).toMutableList()
}

fun Database.enumTypes(action: MutableList<EnumType>.() -> Unit) {
    // FIXME: I don't think this should be cumulative -- it's one shot!!!
    enumTypes = ((enumTypes ?: mutableListOf()).apply(action))
}

fun MutableList<EnumType>.enumType(action: EnumType.() -> Unit) {
    this += EnumType().apply(action)
}

fun Database.enumTypes(vararg types: Pair<String, String>) {
    enumTypes = types.map(Pair<String, String>::toEnumType).toMutableList()
}

fun Database.catalogs(action: MutableList<CatalogMappingType>.() -> Unit) {
    catalogs = ((catalogs ?: mutableListOf()).apply(action))
}

fun MutableList<CatalogMappingType>.catalogMappingType(action: CatalogMappingType.() -> Unit) {
    this += CatalogMappingType().apply(action)
}

fun CatalogMappingType.schemata(action: MutableList<SchemaMappingType>.() -> Unit) {
    schemata = ((schemata ?: mutableListOf()).apply(action))
}

fun MutableList<SchemaMappingType>.schemaMappingType(action: SchemaMappingType.() -> Unit) {
    this += SchemaMappingType().apply(action)
}

fun Database.embeddables(action: MutableList<Embeddable>.() -> Unit) {
    embeddables = ((embeddables ?: mutableListOf()).apply(action))
}

fun MutableList<Embeddable>.embeddable(action: Embeddable.() -> Unit) {
    this += Embeddable().apply(action)
}

fun Embeddable.field(action: EmbeddableField.() -> Unit) {
    getFields() += EmbeddableField().apply(action)
}

fun Embeddable.fields(vararg fields: Pair<String, String>) {
    this.fields = fields.map(Pair<String, String>::toEmbeddableField).toMutableList()
}

internal fun Pair<String, String>.toProperty() = Property().apply {
    key = first
    value = second
}

internal fun Pair<String, String>.toEnumType() = EnumType().apply {
    name = first
    literals = second
}

internal fun Pair<String, String>.toEmbeddableField() = EmbeddableField().apply {
    name = first
    expression = second
}
