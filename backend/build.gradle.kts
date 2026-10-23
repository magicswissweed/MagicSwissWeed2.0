import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    java
    id("idea")
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.flywaydb.flyway") version "9.7.0"
    id("org.openapi.generator") version "7.5.0"
    id("nu.studer.jooq") version "9.0"
}

group = "com.aa"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

// Boot 3.5.16 still manages versions with known CVEs - pin the patched ones.
// Drop these once the Boot BOM catches up.
extra["tomcat.version"] = "10.1.61"           // CVE-2026-65182, -65905, -68525
extra["netty.version"] = "4.1.139.Final"      // CVE-2026-75595

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.flywaydb:flyway-core:9.8.2")
    implementation("org.openapitools:jackson-databind-nullable:0.2.12")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.google.firebase:firebase-admin:9.11.0")
    implementation("org.locationtech.proj4j:proj4j:1.4.1")
    implementation("org.locationtech.proj4j:proj4j-epsg:1.4.1")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly("io.swagger.core.v3:swagger-annotations:2.2.21")
    compileOnly("jakarta.validation:jakarta.validation-api:3.1.0")
    compileOnly("org.projectlombok:lombok:1.18.46")

    annotationProcessor("org.projectlombok:lombok:1.18.46")

    jooqGenerator("org.postgresql:postgresql:42.7.14")

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:postgresql")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val generatedApiDir = "src/generated/api"
java.sourceSets["main"].java {
    srcDir(generatedApiDir)
}

val generatedJooqDir = "src/generated/jooq"
java.sourceSets["main"].java {
    srcDir(generatedJooqDir)
}

val gradlePostgresHost = "localhost:5432"

flyway {
    url = "jdbc:postgresql://$gradlePostgresHost/gradlecodegen"
    user = "gradle"
    password = "password"
    locations = listOf("filesystem:" + project.rootDir + "/backend/src/main/resources/db/migration").toTypedArray()
}

tasks.register<GenerateTask>("openApiGenerateSpring") {
    group = "Msw OpenApi"

    generatorName = "spring"
    inputSpec = "$projectDir/src/main/resources/api/mswApi.yaml"
    outputDir = "$projectDir/$generatedApiDir"
    apiPackage = "com.aa.msw.gen.api"
    modelPackage = "com.aa.msw.gen.api"

    // See https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/spring.md for all options
    configOptions = mapOf(
        "interfaceOnly" to "true",
        "useJakartaEe" to "true",
        "sourceFolder" to "", // With the default main/src/java in generated-src, the package name does not match
        "useTags" to "true",
        "openApiNullable" to "false", // keep nullable fields as plain types instead of wrapping them in JsonNullable<T>
    )

    doLast {
        // OpenAPI generator generates other files, we don't need
        delete(
            "$projectDir/$generatedApiDir/.openapi-generator",
            "$projectDir/$generatedApiDir/.openapi-generator-ignore",
            "$projectDir/$generatedApiDir/pom.xml",
            "$projectDir/$generatedApiDir/README.md",
        )
    }
}

tasks.register<GenerateTask>("openApiGenerateReact") {
    group = "Msw OpenApi"

    generatorName = "typescript-axios"
    inputSpec = "$projectDir/src/main/resources/api/mswApi.yaml"
    outputDir = "$projectDir/../frontend/src/gen/msw-api-ts"

    configOptions = mapOf(
        "npmName" to "msw-api-ts",
        "supportsES6" to "true",
    )
}

jooq {
    // the generator must match the jOOQ runtime that the Spring Boot BOM pins,
    // otherwise codegen dies with AbstractMethodError on a Boot upgrade
    version = dependencyManagement.importedProperties["jooq.version"]
    edition = nu.studer.gradle.jooq.JooqEdition.OSS  // default (can be omitted)

    configurations {
        create("main") {  // name of the jOOQ configuration
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://$gradlePostgresHost/gradlecodegen"
                    user = "gradle"
                    password = "password"
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                    }
                    target.apply {
                        packageName = "com.aa.msw.gen.jooq"
                        directory = "$projectDir/$generatedJooqDir"
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                        isRelations = true
                        isDaos = true
                    }
                }
            }
        }
    }
}

tasks.openApiGenerate {
    doLast {
        // OpenAPI generator generates other files, we don't need
        delete(
            "$projectDir/$generatedApiDir/.openapi-generator",
            "$projectDir/$generatedApiDir/.openapi-generator-ignore",
            "$projectDir/$generatedApiDir/pom.xml",
            "$projectDir/$generatedApiDir/README.md",
        )
    }
}

tasks.named("generateJooq").configure {
    dependsOn("flywayMigrate")
}// JooqGenerator cleansOut the generated dir, so buildAllApi has to build after jooq has.

idea {
    module {
        generatedSourceDirs.add(file(generatedApiDir))
        generatedSourceDirs.add(file(generatedJooqDir))
    }
}
