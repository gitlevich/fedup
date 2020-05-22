val patchVersion: String by project
val baseVersion: String by project

val assertjVersion: String by project
val jupiterVersion: String by project
val mockkVersion: String by project

plugins {
    val kotlinVersion = "1.3.71"

    kotlin("jvm")
    `maven-publish`
    kotlin("plugin.jpa") version kotlinVersion
}

version = "$baseVersion.$patchVersion"

dependencies {
    listOf(
        kotlin("stdlib-jdk8")
    ).forEach { implementation(it) }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

sonarqube {
    properties {
        property("sonar.projectName", "daap-monolith-api")
    }
}
