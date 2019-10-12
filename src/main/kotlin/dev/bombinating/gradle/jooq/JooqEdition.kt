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
package dev.bombinating.gradle.jooq

/**
 * jOOQ edition info.
 *
 * @property groupId Maven group id associated with the jOOQ edition
 * @property pro whether the version is non-OS
 */
enum class JooqEdition(val groupId: String, val pro: Boolean) {
    /**
     * Open source edition.
     */
    OpenSource("org.jooq", false),
    /**
     * Java 9+ Pro edition.
     */
    Pro("org.jooq.pro", true),
    /**
     * Java 8+ Pro edition.
     */
    ProJava8("org.jooq.pro-java-8", true),
    /**
     * Java 6+ Pro edition.
     */
    ProJava6("org.jooq.pro-java-6", true)
}
