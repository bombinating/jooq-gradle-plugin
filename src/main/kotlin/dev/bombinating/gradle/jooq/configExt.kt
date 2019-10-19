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

/**
 * Extension method for customizing the `properties` config in a `Database` config
 *
 * @receiver Parent `Database` the properties associated with
 * @param props pairs of properties
 */
fun Database.properties(vararg props: Pair<String, String>) {
    properties = props.map(Pair<String, String>::toProperty).toMutableList()
}

/**
 * Extension method for customizing the `properties` config in a `Jdbc` config
 *
 * @receiver Parent `Jdbc` the [MutableList] of `Property` config is associated with
 * @param action lambda for customizing the [MutableList] of `Property` config
 */
fun Jdbc.properties(action: MutableList<Property>.() -> Unit) {
    properties = ((properties ?: mutableListOf()).apply(action))
}

/**
 * Extension method for customizing the `properties` config in a `Jdbc` config
 *
 * @receiver Parent `Database` the properties associated with
 * @param props pairs of properties
 */
fun Jdbc.properties(vararg props: Pair<String, String>) {
    properties = props.map(Pair<String, String>::toProperty).toMutableList()
}

/**
 * Extension method for customizing the `enumTypes` config in a `Database` config
 *
 * @receiver Parent `Database` the [MutableList] of [EnumType] config is associated with
 * @param action lambda for customizing the [MutableList] of [EnumType] config
 */
fun Database.enumTypes(action: MutableList<EnumType>.() -> Unit) {
    enumTypes = ((enumTypes ?: mutableListOf()).apply(action))
}

/**
 * Extension method for adding an [EnumType] to a [MutableList].
 *
 * @receiver Parent [MutableList] the [EnumType] is being added to
 * @param action to create an [EnumType] to add to the [MutableList] of enum types
 */
fun MutableList<EnumType>.enumType(action: EnumType.() -> Unit) {
    this += EnumType().apply(action)
}

/**
 * Extension method for adding [String] [Pair]s as [EnumType]s to the [MutableList] of enum types associated with a
 * [Database]
 *
 * @receiver Parent [Database] the [EnumType]s are being added to
 * @param types [String] [Pair]s to turn into [EnumType]s and add to the receiver [Database]
 */
fun Database.enumTypes(vararg types: Pair<String, String>) {
    enumTypes = types.map(Pair<String, String>::toEnumType).toMutableList()
}

/**
 * Extension method for adding a list of [CatalogMappingType]s to the [Database] receiver.
 *
 * @receiver Parent [Database] the [CatalogMappingType]s are being added to
 * @param action create the list of [CatalogMappingType] to add to the [Database] receiver
 */
fun Database.catalogs(action: MutableList<CatalogMappingType>.() -> Unit) {
    catalogs = ((catalogs ?: mutableListOf()).apply(action))
}

/**
 * Extension method for adding a [CatalogMappingType] object to a list.
 *
 * @receiver List of [CatalogMappingType] the [CatalogMappingType] is being added to
 * @param action create the [CatalogMappingType] to add to the list
 */
fun MutableList<CatalogMappingType>.catalogMappingType(action: CatalogMappingType.() -> Unit) {
    this += CatalogMappingType().apply(action)
}

/**
 * Extension method for adding a list of [SchemaMappingType] objects to the list of schemata.
 *
 * @receiver [CatalogMappingType] the [SchemaMappingType] is being added to
 * @param action create the list of [SchemaMappingType] to add to the list of schemata
 */
fun CatalogMappingType.schemata(action: MutableList<SchemaMappingType>.() -> Unit) {
    schemata = ((schemata ?: mutableListOf()).apply(action))
}

/**
 * Extension method for adding a list of [SchemaMappingType] objects to a list
 *
 * @receiver list [SchemaMappingType] is being added to
 * @param action create the [SchemaMappingType] to add to the list
 */
fun MutableList<SchemaMappingType>.schemaMappingType(action: SchemaMappingType.() -> Unit) {
    this += SchemaMappingType().apply(action)
}

/**
 * Extension method for adding a list of [Embeddable] objects to a list
 *
 * @receiver Database the [Embeddable] objects are being added to
 * @param action create the list of [Embeddable] objects to add to the list
 */
fun Database.embeddables(action: MutableList<Embeddable>.() -> Unit) {
    embeddables = ((embeddables ?: mutableListOf()).apply(action))
}

/**
 * Extension method for adding an [Embeddable] object to a list
 *
 * @receiver List of [Embeddable] objects to add to
 * @param action create the [Embeddable] object to add to the list
 */
fun MutableList<Embeddable>.embeddable(action: Embeddable.() -> Unit) {
    this += Embeddable().apply(action)
}

/**
 * Extension method for adding an [EmbeddableField] object to an [Embeddable]
 *
 * @receiver [Embeddable] object to add to
 * @param action create the [EmbeddableField] object to add to the list
 */
fun Embeddable.field(action: EmbeddableField.() -> Unit) {
    getFields() += EmbeddableField().apply(action)
}

/**
 * Extension method for adding String pairs as an [EmbeddableField] objects to an [Embeddable]
 *
 * @receiver [Embeddable] object to add to
 * @param fields String [Pair] objects that are converted into [EmbeddableField] objects
 */
fun Embeddable.fields(vararg fields: Pair<String, String>) {
    this.fields = fields.map(Pair<String, String>::toEmbeddableField).toMutableList()
}

/**
 * Extension method for converting a String [Pair] to a [Property] object
 *
 * @receiver String [Pair]
 * @return [Property] object constructed from the [Pair] values
 */
internal fun Pair<String, String>.toProperty() = Property().apply {
    key = first
    value = second
}

/**
 * Extension method for converting a String [Pair] to an [EnumType] object
 *
 * @receiver String [Pair]
 * @return [EnumType] object constructed from the [Pair] values
 */
internal fun Pair<String, String>.toEnumType() = EnumType().apply {
    name = first
    literals = second
}

/**
 * Extension method for converting a String [Pair] to an [EmbeddableField] object
 *
 * @receiver String [Pair]
 * @return [EmbeddableField] object constructed from the [Pair] values
 */
internal fun Pair<String, String>.toEmbeddableField() = EmbeddableField().apply {
    name = first
    expression = second
}
