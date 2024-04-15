/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.plugin.ProjectLocalConfigurations
import org.jetbrains.kotlin.gradle.testbase.*
import java.util.*
import kotlin.io.path.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@MppGradlePluginTests
class MultiplatformDslAppAndLibJsIT : KGPBaseTest() {

    @GradleTest
    fun testLibAndAppJsIr(gradleVersion: GradleVersion) {
        doTestLibAndAppJsBothCompilers(
            libProjectPath = "new-mpp-lib-and-app/sample-lib",
            appProjectPath = "new-mpp-lib-and-app/sample-app",
            gradleVersion = gradleVersion,
        )
    }

    @GradleTest
    fun testLibAndAppWithGradleKotlinDslJsIr(gradleVersion: GradleVersion) {
        doTestLibAndAppJsBothCompilers(
            libProjectPath = "new-mpp-lib-and-app/sample-lib-gradle-kotlin-dsl",
            appProjectPath = "new-mpp-lib-and-app/sample-app-gradle-kotlin-dsl",
            gradleVersion = gradleVersion,
        )
    }

    private fun doTestLibAndAppJsBothCompilers(
        libProjectPath: String,
        appProjectPath: String,
        gradleVersion: GradleVersion,
    ) {
//            val libProject = transformProjectWithPluginsDsl(libProjectName, directoryPrefix = "both-js-lib-and-app")
//            val appProject = transformProjectWithPluginsDsl(appProjectName, directoryPrefix = "both-js-lib-and-app")

        val compileTasksNames = listOf(":compileKotlinNodeJs")

        val libProject = project(
            projectName = libProjectPath,
            gradleVersion = gradleVersion,
        ) {
            gradleProperties.append(
                """
                kotlin.compiler.execution.strategy=in-process
                """.trimIndent()
            )
            build(
                "publish",
//                    options = defaultBuildOptions()
            ) {
                assertTasksAreNotInTaskGraph(":compileCommonMainKotlinMetadata")
                assertTasksExecuted(compileTasksNames)
                assertTasksExecuted(":allMetadataJar")

                val groupDir = projectPath.resolve("repo/com/example")

                val jsExtension = "klib"
                val jsJarName = "sample-lib-nodejs/1.0/sample-lib-nodejs-1.0.$jsExtension"
                val metadataJarName = "sample-lib/1.0/sample-lib-1.0.jar"

                listOf(jsJarName, metadataJarName, "sample-lib/1.0/sample-lib-1.0.module").forEach {
                    assertFileExists(groupDir.resolve(it))
                }

                val gradleMetadata = groupDir.resolve("sample-lib/1.0/sample-lib-1.0.module").readText()
                assertFalse(gradleMetadata.contains(ProjectLocalConfigurations.ATTRIBUTE.name))

                jsJarName.let {
                    val pom = groupDir.resolve(it.replaceAfterLast('.', "pom"))
                    assertFileContains(
                        pom,
                        "<name>Sample MPP library</name>",
                    )
                    assertFileDoesNotContain(
                        pom,
                        "<groupId>Kotlin/Native</groupId>",
                        message = "$pom should not contain standard K/N libraries as dependencies.",
                    )
                }

                groupDir.resolve(jsJarName).exists()
            }
        }

        val libLocalRepo = libProject.projectPath.resolve("repo")

        project(
            projectName = appProjectPath,
            gradleVersion = gradleVersion,
            localRepoDir = libLocalRepo,
        ) {
            gradleProperties.append(
                """
                    kotlin.compiler.execution.strategy=in-process
                    """.trimIndent()
            )

            fun BuildResult.checkAppBuild() {
                assertTasksExecuted(compileTasksNames)
            }

            build(
                "assemble",
//                    options = defaultBuildOptions()
            ) {
                checkAppBuild()
            }

            // Now run again with a project dependency instead of a module one:
            includeOtherProjectAsSubmodule(
                otherProjectName = libProjectPath,
                newSubmoduleName = libProject.projectPath.name,
                isKts = settingsGradleKts.exists(),
            )
            // Delete the lib local repo, to ensure that Gradle uses the subproject
            libLocalRepo.deleteRecursively()

            val buildScript = if (buildGradle.exists()) buildGradle else buildGradleKts
            buildScript.modify {
                it.replace("\"com.example:sample-lib:1.0\"", "project(\":${libProject.projectPath.name}\")")
            }

            build(
                "assemble",
                "--rerun-tasks",
//                    options = defaultBuildOptions()
            ) {
                assertTasksExecuted(":${libProject.projectPath.name}:assemble")
                checkAppBuild()
            }
        }
    }
}
