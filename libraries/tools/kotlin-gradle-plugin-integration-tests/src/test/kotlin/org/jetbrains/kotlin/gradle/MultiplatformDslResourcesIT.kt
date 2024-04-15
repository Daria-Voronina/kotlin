/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.gradle

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.testbase.*

@MppGradlePluginTests
class MultiplatformDslResourcesIT : KGPBaseTest() {

    @GradleTest
    fun testResourceProcessing(gradleVersion: GradleVersion) {
        project(
            projectName = "new-mpp-lib-and-app/sample-lib",
            gradleVersion = gradleVersion,
        ) {
            val targetsWithResources = listOf("jvm6", "nodeJs", "linux64")
            val processResourcesTasks = targetsWithResources.map { ":${it}ProcessResources" }

            build(
                buildArguments = processResourcesTasks.toTypedArray()
            ) {
                assertTasksExecuted(processResourcesTasks)

                targetsWithResources.forEach {
                    assertFileExists(projectPath.resolve("build/processedResources/$it/main/commonMainResource.txt"))
                    assertFileExists(projectPath.resolve("build/processedResources/$it/main/${it}MainResource.txt"))
                }
            }
        }
    }

}
