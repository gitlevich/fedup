val coverageThreshold = 0.60

val assertjVersion: String by project
val awaitilityVersion: String by project
val h2Version: String by project
val jacksonModuleKotlinVersion: String by project
val jacocoToolVersion: String by project
val jcraftVersion: String by project
val jupiterVersion: String by project
val mockkVersion: String by project
val springMockkVersion: String by project
val swaggerVersion: String by project
val plaidClientVersion: String by project

plugins {
    val kotlinVersion = "1.3.71"

    application
    kotlin("jvm")
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.noarg") version kotlinVersion
    kotlin("kapt")
    id("org.springframework.boot") version "2.2.0.RELEASE"
    id("com.google.cloud.tools.jib") version "1.6.1"
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    // Implementation
    listOf(
        project(":shared"),
        kotlin("stdlib-jdk8"),
        kotlin("reflect"),
        "org.springframework.boot:spring-boot-starter-web",
        "org.springframework.boot:spring-boot-starter-data-jpa",
        "org.springframework.boot:spring-boot-starter-actuator",
        "org.springframework.kafka:spring-kafka",
        "com.jcraft:jsch:$jcraftVersion",
        "com.fasterxml.jackson.module:jackson-module-kotlin",
        "com.fasterxml.jackson.datatype:jackson-datatype-jsr310",
        "com.fasterxml.jackson.dataformat:jackson-dataformat-xml",
        "io.springfox:springfox-swagger2:$swaggerVersion",
        "io.springfox:springfox-swagger-ui:$swaggerVersion"
    ).forEach { implementation(it) }

    // Runtime
    listOf(
        "com.h2database:h2:$h2Version"
    )
        .forEach { runtime(it) }

    // Test Implementation
    listOf(
        "com.ninja-squad:springmockk:$springMockkVersion",
        "org.awaitility:awaitility:$awaitilityVersion",
        "org.junit.vintage:junit-vintage-engine:$jupiterVersion",
        "org.springframework.boot:spring-boot-starter-test",
        "org.springframework.kafka:spring-kafka-test",
        "org.springframework.security:spring-security-test"
    ).forEach { testImplementation(it) }

    // Test Runtime
    listOf(
        kotlin("reflect"),
        "org.junit.jupiter:junit-jupiter-engine:$jupiterVersion",
        "org.junit.vintage:junit-vintage-engine:$jupiterVersion"
    )
        .forEach { testRuntime(it) }

    kapt("org.springframework.boot:spring-boot-configuration-processor")
}

application {
    mainClassName = "com.group1001.daap.ApplicationKt"
}

jacoco {
    toolVersion = jacocoToolVersion
}

sonarqube {
    properties {
        property("sonar.projectName", "daap-monolith-app")
    }
}

tasks {
    named("check") {
        dependsOn("jacocoTestCoverageVerification")
    }
    withType<Test> {
        useJUnitPlatform()
    }
}

noArg {
    annotation("org.axonframework.spring.stereotype.Aggregate")
}

configurations {
    all {
        exclude("com.vaadin.external.google", "android-json")
    }
}
