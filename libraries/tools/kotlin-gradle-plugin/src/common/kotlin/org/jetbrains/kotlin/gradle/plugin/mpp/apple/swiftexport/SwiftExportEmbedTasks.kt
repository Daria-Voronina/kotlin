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
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.BuildSPMSwiftExportPackage
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.CopySwiftExportIntermediatesForConsumer
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.GenerateSPMPackageFromSwiftExport
import org.jetbrains.kotlin.gradle.tasks.dependsOn
import org.jetbrains.kotlin.gradle.tasks.locateOrRegisterTask
import org.jetbrains.kotlin.gradle.utils.mapToFile

internal fun Project.registerSwiftExportEmbedPipelineTask(
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
        registerCopyTask(
            taskNamePrefix = taskNamePrefix,
            staticLibrary = staticLibrary,
            packageGenerationTask = packageGenerationTask,
            packageBuildTask = packageBuild
        )
    }
}

private fun Project.registerCopyTask(
    taskNamePrefix: String,
    staticLibrary: AbstractNativeLibrary,
    packageGenerationTask: TaskProvider<GenerateSPMPackageFromSwiftExport>,
    packageBuildTask: TaskProvider<BuildSPMSwiftExportPackage>,
): TaskProvider<out Task> {
    val copyTaskName = taskNamePrefix + "CopySPMIntermediates"
    val copyTask = locateOrRegisterTask<CopySwiftExportIntermediatesForConsumer>(copyTaskName) { task ->
        task.group = BasePlugin.BUILD_GROUP
        task.description = "Copy $taskNamePrefix SPM intermediates"

        // Input
        task.includeBridgeDirectory.set(layout.file(packageGenerationTask.map { it.headerBridgeIncludePath }))
        task.includeKotlinRuntimeDirectory.set(layout.file(packageGenerationTask.map { it.kotlinRuntimeIncludePath }))
        task.kotlinLibraryPath.set(layout.file(staticLibrary.linkTaskProvider.flatMap { it.outputFile }))
        task.packageLibraryPath.set(layout.file(packageBuildTask.map { it.packageLibraryPath }))
        task.packageInterfacesPath.set(layout.file(packageBuildTask.map { it.interfacesPath }))
    }
    copyTask.dependsOn(packageBuildTask)
    return copyTask
}

