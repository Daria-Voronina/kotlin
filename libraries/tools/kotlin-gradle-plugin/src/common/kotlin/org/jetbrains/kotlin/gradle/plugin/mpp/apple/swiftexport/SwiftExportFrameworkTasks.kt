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
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.internal.SwiftExportConstants
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.*
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.BuildSPMSwiftExportPackage
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.GenerateSPMPackageFromSwiftExport
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.HeaderDefinition
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.LibraryDefinition
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.SwiftExportFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.dependsOn
import org.jetbrains.kotlin.gradle.tasks.locateOrRegisterTask
import org.jetbrains.kotlin.gradle.utils.konanDistribution
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
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
    ) { packageBuild, packageGenerationTask, mergeLibrariesTask ->

        registerProduceSwiftExportFrameworkTask(
            buildType = buildType,
            target = target,
            swiftApiModuleName = swiftApiModuleName,
            packageGenerationTask = packageGenerationTask,
            mergeLibrariesTask = mergeLibrariesTask,
            packageBuildTask = packageBuild
        )
    }
}

private fun Project.registerProduceSwiftExportFrameworkTask(
    buildType: NativeBuildType,
    target: KotlinNativeTarget,
    swiftApiModuleName: Provider<String>,
    packageGenerationTask: TaskProvider<GenerateSPMPackageFromSwiftExport>,
    mergeLibrariesTask: TaskProvider<MergeStaticLibrariesTask>,
    packageBuildTask: TaskProvider<BuildSPMSwiftExportPackage>,
): TaskProvider<out Task> {

    val configuration = buildType.getName()
    val frameworkTaskName = lowerCamelCaseName(
        "assemble",
        configuration,
        "swiftExportFramework"
    )

    val library = mergeLibrariesTask.flatMap { it.library.mapToFile() }
    val swiftModule = packageBuildTask.map { it.swiftModulePath }
    val headerDefinition = packageHeaderDefinitions(packageGenerationTask)

    val frameworkTask = locateOrRegisterTask<SwiftExportFrameworkTask>(frameworkTaskName) { task ->
        task.group = BasePlugin.BUILD_GROUP
        task.description = "Creates Swift Export ${configuration.capitalize()} Apple Framework"

        // Input
        task.binaryName.set(swiftApiModuleName)
        task.headerDefinitions.set(headerDefinition)

        // Output
        task.frameworkRoot.set(layout.buildDirectory.dir("SwiftExportFramework/${configuration.capitalize()}"))
    }.also {
        it.configure { task ->
            task.library(
                provider {
                    LibraryDefinition(
                        target.name,
                        library.get(),
                        swiftModule.get(),
                    )
                },
                target.konanTarget
            )
        }
    }

    frameworkTask.dependsOn(mergeLibrariesTask)
    frameworkTask.dependsOn(packageBuildTask)

    return frameworkTask
}

private fun Project.packageHeaderDefinitions(
    packageGenerationTask: TaskProvider<GenerateSPMPackageFromSwiftExport>,
): Provider<List<HeaderDefinition>> {
    return packageGenerationTask.map { packageTask ->
        val swiftExportModule = HeaderDefinition(
            packageTask.headerBridgeModuleName.get(),
            packageTask.headerBridgePath.asFile.get()
        )

        val kotlinModule = HeaderDefinition(
            SwiftExportConstants.KOTLIN_RUNTIME,
            file(Distribution(konanDistribution.root.absolutePath).kotlinRuntimeForSwiftHeader)
        )

        listOf(swiftExportModule, kotlinModule)
    }
}