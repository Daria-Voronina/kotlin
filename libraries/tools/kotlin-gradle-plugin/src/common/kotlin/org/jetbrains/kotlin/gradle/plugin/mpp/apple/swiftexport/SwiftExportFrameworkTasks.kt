/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractNativeLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.internal.SwiftExportFrameworkTask
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.internal.ModuleDefinition
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.internal.SwiftExportConstants
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.BuildSPMSwiftExportPackage
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.GenerateSPMPackageFromSwiftExport
import org.jetbrains.kotlin.gradle.tasks.dependsOn
import org.jetbrains.kotlin.gradle.tasks.locateOrRegisterTask
import org.jetbrains.kotlin.gradle.utils.konanDistribution
import org.jetbrains.kotlin.gradle.utils.mapToFile
import org.jetbrains.kotlin.konan.target.Distribution

internal fun Project.registerSwiftExportFrameworkPipelineTask(
    swiftApiModuleName: Provider<String>,
    taskNamePrefix: String,
    target: KotlinNativeTarget,
    buildType: NativeBuildType,
): TaskProvider<out Task> {
    return setupCommonSwiftExportPipeline(
        swiftApiModuleName = swiftApiModuleName,
        taskNamePrefix = taskNamePrefix,
        target = target,
        buildType = buildType
    ) { packageBuild, packageGenerationTask, staticLibrary ->
        registerProduceSwiftExportFrameworkTask(
            taskNamePrefix = taskNamePrefix,
            staticLibrary = staticLibrary,
            swiftApiModuleName = swiftApiModuleName,
            packageGenerationTask = packageGenerationTask,
            packageBuildTask = packageBuild
        )
    }
}

private fun Project.registerProduceSwiftExportFrameworkTask(
    taskNamePrefix: String,
    staticLibrary: AbstractNativeLibrary,
    swiftApiModuleName: Provider<String>,
    packageGenerationTask: TaskProvider<GenerateSPMPackageFromSwiftExport>,
    packageBuildTask: TaskProvider<BuildSPMSwiftExportPackage>,
): TaskProvider<SwiftExportFrameworkTask> {
    val frameworkTaskName = taskNamePrefix + "SwiftExportFramework"
    val createFramework = locateOrRegisterTask<SwiftExportFrameworkTask>(frameworkTaskName) { task ->

        val headers = packageGenerationTask.map { packageTask ->
            val swiftExportModule = ModuleDefinition(
                packageTask.headerBridgeModuleName.get(),
                packageTask.headerBridgePath.asFile.get()
            )

            val kotlinModule = ModuleDefinition(
                SwiftExportConstants.KOTLIN_RUNTIME,
                file(Distribution(konanDistribution.root.absolutePath).kotlinRuntimeForSwiftHeader)
            )

            listOf(swiftExportModule, kotlinModule)
        }
        val mainLibrary = staticLibrary.linkTaskProvider.flatMap { it.outputFile }
        val spmLibrary = packageBuildTask.flatMap { it.packageLibraryPath.mapToFile() }
        val swiftModule = packageBuildTask.flatMap { it.swiftModulePath }

        task.group = BasePlugin.BUILD_GROUP
        task.description = "Creates $taskNamePrefix Swift Export Apple Framework"
        task.workingDir.set(layout.buildDirectory)
        task.binaryName.set(swiftApiModuleName)
        task.libraries.from(mainLibrary, spmLibrary)
        task.swiftModule.set(swiftModule)
        task.headerDefinitions.set(headers)
    }

    createFramework.dependsOn(staticLibrary.linkTaskProvider)
    createFramework.dependsOn(packageBuildTask)
    return createFramework
}