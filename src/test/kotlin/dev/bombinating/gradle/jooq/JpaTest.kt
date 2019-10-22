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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class JpaTest {

    @TempDir
    lateinit var workspaceDir: Path

    private val config = TestConfig(
        driver = h2Driver,
        url = h2Url,
        username = h2Username,
        password = h2Password,
        schema = defaultSchemaName,
        genDir = defaultGenDir,
        javaVersion = "JavaVersion.VERSION_1_8",
        version = jooqVersion12,
        packageName = "dev.bombinating.gradle.jooq.entity.db",
        dbGenerator = """includes = ".*"""",
        addSchemaToPackage = false
    )

    private val deps = """
        |compileOnly("javax.annotation:javax.annotation-api:1.3.2")
        |compile(group = "org.hibernate.javax.persistence", name = "hibernate-jpa-2.1-api", version = "1.0.0.Final")
        |jooqRuntime(files("${'$'}buildDir/classes/java/main"))
        """.trimMargin()

    private val sourcePackage = "dev.bombinating.gradle.jooq.entity"
    private val className = "Person"

    @Test
    fun `Test Generate jOOQ code from JPA Entities using extension`() {
        createPersonClass()
        workspaceDir.createPropFile()
        workspaceDir.createSettingsFile(projectName = defaultProjectName)
        workspaceDir.createBuildFile(config = config, depBlock = deps) {
            """
            |jooq {
            |   $jpaGeneratorConfig
            |}
            """.trimMargin("|")
        }
        val result = runGradle(workspaceDir, "clean", "build", "jooq", "--info", "--stacktrace")
        validateGradleOutput(workspaceDir = workspaceDir, config = config, result = result, taskName = "jooq")
    }

    @Test
    fun `Test Generate jOOQ code from JPA Entities using task`() {
        createPersonClass()
        workspaceDir.createPropFile()
        workspaceDir.createSettingsFile(projectName = defaultProjectName)
        workspaceDir.createBuildFile(config = config, depBlock = deps) {
            """
            |tasks.register<JooqTask>("jooqJpa") {
            |   $jpaGeneratorConfig
            |}
            """.trimMargin("|")
        }
        val result = runGradle(workspaceDir, "clean", "build", "jooqJpa", "--info", "--stacktrace")
        validateGradleOutput(workspaceDir = workspaceDir, config = config, result = result, taskName = "jooqJpa")
    }

    private val jpaGeneratorConfig =
            """
            |   generator {
            |       database {
            |           name = "org.jooq.meta.extensions.jpa.JPADatabase"
            |           inputSchema = "PUBLIC"
            |           properties {
            |               property("packages" to "$sourcePackage")
            |               property("useAttributeConverters" to "true")
            |               property("unqualifiedSchema" to "true")
            |           }
            |       }
            |       target {
            |           directory = "${config.genDir}"
            |           packageName = "${config.packageName}"
            |       }
            |       logging = Logging.DEBUG
            |   }
            """.trimMargin("|")


    private fun createPersonClass() {
        val file = workspaceDir.toFile()
        val packageDir = File(file, "src/main/java/${sourcePackage.replace(".", "/")}")
        packageDir.mkdirs()
        val javaClass = File(packageDir, "$className.java")
        javaClass.writeText(personEntityCode)
    }

    private val personEntityCode = """
        package $sourcePackage;
        
        import javax.persistence.Column;
        import javax.persistence.Entity;
        import javax.persistence.Id;
        import javax.persistence.Table;
        import java.io.Serializable;
        
        @Entity
        @Table(name = "PERSON")
        public class $className implements Serializable {
        
            @Id
            private String id;
            
            @Column(name = "person_first_name", nullable = false)
            private String firstName;
            
            @Column(name = "middle_name")
            private String middleName;
            
            @Column(name = "surname", nullable = false)
            private String lastName;
        
            public Person() {
            }
        
            public String getId() {
                return id;
            }
        
            public void setId(String id) {
                this.id = id;
            }
        
            public String getFirstName() {
                return firstName;
            }
        
            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }
        
            public String getMiddleName() {
                return middleName;
            }
        
            public void setMiddleName(String middleName) {
                this.middleName = middleName;
            }
        
            public String getLastName() {
                return lastName;
            }
        
            public void setLastName(String lastName) {
                this.lastName = lastName;
            }
        }
    """.trimMargin()

}