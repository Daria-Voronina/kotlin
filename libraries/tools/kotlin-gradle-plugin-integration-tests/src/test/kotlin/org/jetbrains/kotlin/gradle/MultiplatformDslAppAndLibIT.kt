/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.gradle

import org.gradle.api.logging.LogLevel
import org.gradle.testkit.runner.BuildResult
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.MultiplatformDslAppAndLibIT.HmppFlags.Companion.hmppWoCompatibilityMetadataArtifact
import org.jetbrains.kotlin.gradle.MultiplatformDslAppAndLibIT.HmppFlags.Companion.noHMPP
import org.jetbrains.kotlin.gradle.plugin.ProjectLocalConfigurations
import org.jetbrains.kotlin.gradle.testbase.*
import org.jetbrains.kotlin.gradle.util.isWindows
import java.util.*
import java.util.zip.ZipFile
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.test.assertTrue

@MppGradlePluginTests
class MultiplatformDslAppAndLibIT : KGPBaseTest() {

    private data class HmppFlags(
        val hmppSupport: Boolean,
        val name: String,
    ) {
        val args = buildList {
            if (hmppSupport) {
                add("-Pkotlin.mpp.hierarchicalStructureSupport")
                add("-Pkotlin.internal.suppressGradlePluginErrors=PreHMPPFlagsError")
            }
        }

        override fun toString() = name

        companion object {
            val noHMPP = HmppFlags(
                name = "No HMPP",
                hmppSupport = false
            )

            val hmppWoCompatibilityMetadataArtifact = HmppFlags(
                name = "HMPP without Compatibility Metadata Artifact",
                hmppSupport = true
            )
        }
    }

    @GradleTest
    fun testLibAndApp(gradleVersion: GradleVersion) {
        doTestLibAndApp(
            "new-mpp-lib-and-app/sample-lib",
            "new-mpp-lib-and-app/sample-app",
            hmppWoCompatibilityMetadataArtifact,
            gradleVersion = gradleVersion,
        )
    }

    @GradleTest
    fun testLibAndAppWithoutHMPP(gradleVersion: GradleVersion) = doTestLibAndApp(
        libProjectPath = "new-mpp-lib-and-app/sample-lib",
        appProjectPath = "new-mpp-lib-and-app/sample-app",
        hmppFlags = noHMPP,
        gradleVersion = gradleVersion,
    )

    @GradleTest
    fun testLibAndAppWithGradleKotlinDsl(gradleVersion: GradleVersion) {
        doTestLibAndApp(
            "new-mpp-lib-and-app/sample-lib-gradle-kotlin-dsl",
            "new-mpp-lib-and-app/sample-app-gradle-kotlin-dsl",
            hmppWoCompatibilityMetadataArtifact,
            gradleVersion = gradleVersion,
        )
    }

    private fun doTestLibAndApp(
        libProjectPath: String,
        appProjectPath: String,
        hmppFlags: HmppFlags,
        gradleVersion: GradleVersion,
    ) {
//        val libProject = transformNativeTestProjectWithPluginDsl(libProjectName, directoryPrefix = "new-mpp-lib-and-app")
//        val appProject = transformNativeTestProjectWithPluginDsl(appProjectName, directoryPrefix = "new-mpp-lib-and-app")

        val compileTasksNames = listOf(
            ":compileKotlinJvm6",
            ":compileKotlinNodeJs",
            ":compileKotlinLinux64",
        )

        val libProject = project(
            projectName = libProjectPath,
            gradleVersion = gradleVersion,
        ) {
            build(
                buildArguments = buildList {
                    add("publish")
                    addAll(hmppFlags.args)
                }.toTypedArray(),
            ) {
                assertTasksExecuted(compileTasksNames)
                assertTasksExecuted(
                    ":jvm6Jar",
                    ":nodeJsJar",
                    ":compileCommonMainKotlinMetadata",
                )

                val groupDir = projectPath.resolve("repo/com/example")
                val jvmJarName = "sample-lib-jvm6/1.0/sample-lib-jvm6-1.0.jar"
                val jsExtension = "klib"
                val jsKlibName = "sample-lib-nodejs/1.0/sample-lib-nodejs-1.0.$jsExtension"
                val metadataJarName = "sample-lib/1.0/sample-lib-1.0.jar"
                val nativeKlibName = "sample-lib-linux64/1.0/sample-lib-linux64-1.0.klib"

                listOf(jvmJarName, jsKlibName, metadataJarName, "sample-lib/1.0/sample-lib-1.0.module").forEach {
                    assertFileExists(groupDir.resolve(it))
                }

                val gradleMetadata = groupDir.resolve("sample-lib/1.0/sample-lib-1.0.module")
                assertFileDoesNotContain(gradleMetadata, ProjectLocalConfigurations.ATTRIBUTE.name)

                listOf(jvmJarName, jsKlibName, nativeKlibName).forEach {
                    val pom = groupDir.resolve(it.replaceAfterLast('.', "pom"))
                    assertFileContains(
                        pom,
                        "<name>Sample MPP library</name>",
                    )
                    assertFileDoesNotContain(
                        pom,
                        "<groupId>Kotlin/Native</groupId>",
                        message = "$pom should not contain standard K/N libraries as dependencies."
                    )
                }

                val jvmJarEntries = ZipFile(groupDir.resolve(jvmJarName).toFile()).entries().asSequence().map { it.name }.toSet()
                assertTrue("com/example/lib/CommonKt.class" in jvmJarEntries)
                assertTrue("com/example/lib/MainKt.class" in jvmJarEntries)

                assertFileExists(groupDir.resolve(jsKlibName))

                assertFileExists(groupDir.resolve(nativeKlibName))
            }
        }

        val libLocalRepo = libProject.projectPath.resolve("repo")

        project(
            projectName = appProjectPath,
            gradleVersion = gradleVersion,
            localRepoDir = libLocalRepo,
        ) {
            fun BuildResult.checkAppBuild() {
                assertTasksExecuted(compileTasksNames)
                assertTasksExecuted(
                    ":linkMainDebugExecutableLinux64"
                )

                projectPath.resolve(targetClassesDir("jvm6")).run {
                    assertFileExists(resolve("com/example/app/AKt.class"))
                    assertFileExists(resolve("com/example/app/UseBothIdsKt.class"))
                }

                projectPath.resolve(targetClassesDir("jvm8")).run {
                    assertFileExists(resolve("com/example/app/AKt.class"))
                    assertFileExists(resolve("com/example/app/UseBothIdsKt.class"))
                    assertFileExists(resolve("com/example/app/Jdk8ApiUsageKt.class"))
                }

                val nativeExeName = if (isWindows) "main.exe" else "main.kexe"
                assertFileExists(projectPath.resolve("build/bin/linux64/mainDebugExecutable/$nativeExeName"))

                // Check that linker options were correctly passed to the K/N compiler.
                // old way?
//                withNativeCommandLineArguments(":linkMainDebugExecutableLinux64") { arguments ->
//                    val parsedArguments = parseCommandLineArguments<K2NativeCompilerArguments>(arguments)
//                    assertEquals(listOf("-L."), parsedArguments.singleLinkerArguments?.toList())
//                    arguments.assertContainsInOrder("-linker-option", "-L.")
//                }
                // new way?
                extractNativeTasksCommandLineArgumentsFromOutput(
                    ":linkMainDebugExecutableLinux64",
                    logLevel = LogLevel.DEBUG,
                ) {
                    assertCommandLineArgumentsContainSequentially("-linker-option", "-L.")
                }
            }

            build(
                buildArguments = buildList {
                    add("assemble")
                    add("resolveRuntimeDependencies")
                    addAll(hmppFlags.args)
                }.toTypedArray(),
                buildOptions = buildOptions.copy(logLevel = LogLevel.DEBUG),
            ) {
                checkAppBuild()
                assertTasksExecuted(":resolveRuntimeDependencies") // KT-26301
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

            build("clean")

            build(
                "assemble",
                "--rerun-tasks",
                buildOptions = buildOptions.copy(logLevel = LogLevel.DEBUG),
            ) {
                assertTasksExecuted(":${libProject.projectPath.name}:assemble")
                checkAppBuild()
            }
        }
    }

    companion object {
        private fun targetClassesDir(targetName: String, sourceSetName: String = "main") =
            classesDir(sourceSet = "$targetName/$sourceSetName")

        private fun classesDir(subproject: String? = null, sourceSet: String = "main", language: String = "kotlin"): String =
            (subproject?.plus("/") ?: "") + "build/classes/$language/$sourceSet/"


        private fun BuildResult.withNativeCommandLineArguments(
            vararg taskPaths: String,
            toolName: NativeToolKind = NativeToolKind.KONANC,
            check: (arguments: List<String>) -> Unit,
        ) {
            taskPaths.forEach { taskPath ->
                val taskOutput = getOutputForTask(taskPath)
                val arguments = extractNativeCompilerCommandLineArguments(taskOutput, toolName)
                check(arguments)
            }
        }

        private fun List<String>.assertContainsInOrder(vararg expectedElements: String) {
            check(expectedElements.isNotEmpty()) { "expectedElements must not be empty" }

            assertTrue(
                Collections.indexOfSubList(this, expectedElements.toList()) != -1,
                "List $this did not contain elements in order: $expectedElements"
            )
        }
    }
}
