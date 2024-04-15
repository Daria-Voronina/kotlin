package org.jetbrains.kotlin.gradle

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.testbase.*

@MppGradlePluginTests
class MultiplatformDslMavenPublishIT : KGPBaseTest() {

    @GradleTest
    fun testMavenPublishAppliedBeforeMultiplatformPlugin(gradleVersion: GradleVersion) {
        project(
            projectName = "new-mpp-lib-and-app/sample-lib",
            gradleVersion = gradleVersion,
        ) {
            buildGradle.modify {
                val pluginsBlockStart = it.indexOf("plugins {")
                val pluginsBlockEnd = it.indexOf("}", startIndex = pluginsBlockStart)
                val withoutPlugins = it.removeRange(pluginsBlockStart..pluginsBlockEnd)
                """
                    |buildscript {
                    |    repositories {
                    |        mavenLocal()
                    |        mavenCentral()
                    |    }
                    |    dependencies {
                    |        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:${'$'}kotlin_version"
                    |    }
                    |}
                    |
                    |apply plugin: 'maven-publish'
                    |apply plugin: 'kotlin-multiplatform'
                    |
                    |$withoutPlugins
                    |
                """.trimMargin()
            }
            build("help") {
                assertTasksExecuted(":help")
            }
        }
    }
}
