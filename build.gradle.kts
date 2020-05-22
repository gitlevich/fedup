import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val assertjVersion: String by project
val detektToolVersion: String by project
val gradleVersion: String by project
val jupiterVersion: String by project
val mockkVersion: String by project
val ratedVersion: String by project

plugins {
    val kotlinVersion = "1.3.71"

    base
    kotlin("jvm") version kotlinVersion apply false
    id("com.github.nwillc.vplugin") version "3.0.1" apply false
    id("org.sonarqube") version "2.8" apply false
}

allprojects {
    group = "com.fedup"
    version = "0.0.1"

    repositories {
        jcenter()
        mavenLocal()
    }
}

subprojects {
    val project = this

    apply {
        plugin("kotlin")
        plugin("com.github.nwillc.vplugin")
        plugin("org.sonarqube")
        plugin("jacoco")
    }

    val kotlinTest by configurations.creating

    dependencies {
        listOf(
            "io.mockk:mockk:$mockkVersion",
            "org.junit.jupiter:junit-jupiter:$jupiterVersion",
            "org.assertj:assertj-core:$assertjVersion"
        ).forEach { kotlinTest(it) }
    }

    tasks {
        named("sonarqube") {
            dependsOn("jacocoTestReport")
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
        withType<JacocoReport> {
            dependsOn("test")
            reports {
                xml.apply {
                    isEnabled = true
                }
                html.apply {
                    isEnabled = true
                }
            }
        }
        withType<Test> {
            useJUnitPlatform()
            testLogging {
                showStandardStreams = true
                events("passed", "skipped", "failed")
            }
        }
        withType<Wrapper> {
            this.gradleVersion = gradleVersion //version required
        }
    }
}

dependencies {
    // Make the root project archives configuration depend on every subproject
    subprojects.forEach {
        archives(it)
    }
}
