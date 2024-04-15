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
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.MergeStaticLibrariesTask
import org.jetbrains.kotlin.gradle.tasks.dependsOn
import org.jetbrains.kotlin.gradle.tasks.locateOrRegisterTask
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
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
    ) { packageBuild, packageGenerationTask, mergeLibrariesTask ->
        registerCopyTask(
            buildType = buildType,
            packageGenerationTask = packageGenerationTask,
            packageBuildTask = packageBuild,
            mergeLibrariesTask = mergeLibrariesTask
        )
    }
}

private fun Project.registerCopyTask(
    buildType: NativeBuildType,
    packageGenerationTask: TaskProvider<GenerateSPMPackageFromSwiftExport>,
    packageBuildTask: TaskProvider<BuildSPMSwiftExportPackage>,
    mergeLibrariesTask: TaskProvider<MergeStaticLibrariesTask>,
): TaskProvider<out Task> {

    val configuration = buildType.getName()
    val copyTaskName = lowerCamelCaseName(
        "copy",
        configuration,
        "SPMIntermediates"
    )

    val copyTask = locateOrRegisterTask<CopySwiftExportIntermediatesForConsumer>(copyTaskName) { task ->
        task.group = BasePlugin.BUILD_GROUP
        task.description = "Copy ${configuration.capitalize()} SPM intermediates"

        // Input
        task.includeBridgeDirectory.set(layout.file(packageGenerationTask.map { it.headerBridgeIncludePath }))
        task.includeKotlinRuntimeDirectory.set(layout.file(packageGenerationTask.map { it.kotlinRuntimeIncludePath }))
    }

    copyTask.configure { task ->
        task.addLibrary(
            mergeLibrariesTask.flatMap { it.library }.mapToFile()
        )

        task.addInterface(
            packageBuildTask.map { it.interfacesPath }
        )
    }

    copyTask.dependsOn(packageBuildTask)
    copyTask.dependsOn(mergeLibrariesTask)

    return copyTask
}

