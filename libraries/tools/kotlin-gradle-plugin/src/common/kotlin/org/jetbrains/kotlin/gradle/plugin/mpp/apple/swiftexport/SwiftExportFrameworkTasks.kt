/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractNativeLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.SwiftExportFrameworkTask
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.ModuleDefinition
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.internal.SwiftExportConstants
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.BuildSPMSwiftExportPackage
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.GenerateSPMPackageFromSwiftExport
import org.jetbrains.kotlin.gradle.tasks.dependsOn
import org.jetbrains.kotlin.gradle.tasks.locateOrRegisterTask
import org.jetbrains.kotlin.gradle.utils.konanDistribution
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
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
            buildType = buildType,
            staticLibrary = staticLibrary,
            swiftApiModuleName = swiftApiModuleName,
            packageGenerationTask = packageGenerationTask,
            packageBuildTask = packageBuild
        )
    }
}

private fun Project.registerProduceSwiftExportFrameworkTask(
    buildType: NativeBuildType,
    staticLibrary: AbstractNativeLibrary,
    swiftApiModuleName: Provider<String>,
    packageGenerationTask: TaskProvider<GenerateSPMPackageFromSwiftExport>,
    packageBuildTask: TaskProvider<BuildSPMSwiftExportPackage>,
): TaskProvider<out Task> {

    val frameworkTaskName = lowerCamelCaseName(
        "assemble",
        buildType.getName(),
        "swiftExportFramework"
    )

    val frameworkTas = locateOrRegisterTask<SwiftExportFrameworkTask>(frameworkTaskName) { task ->
        task.description = "Creates Swift Export Apple Framework"

        // Input
        task.binaryName.set(swiftApiModuleName)

        task.libraries.from(
            staticLibrary.linkTaskProvider.flatMap { it.outputFile },
            packageBuildTask.map { it.packageLibraryPath }
        )

        task.swiftModule.set(
            layout.dir(
                packageBuildTask.map { it.swiftModulePath }
            )
        )

        task.headerDefinitions.set(packageModuleDefinitions(packageGenerationTask))

        // Output
        task.frameworkRoot.set(layout.buildDirectory.dir("SwiftExportFramework/${buildType.getName().capitalize()}"))
    }

    frameworkTas.dependsOn(staticLibrary.linkTaskProvider)
    frameworkTas.dependsOn(packageBuildTask)

    return frameworkTas
}

private fun Project.packageModuleDefinitions(
    packageGenerationTask: TaskProvider<GenerateSPMPackageFromSwiftExport>,
): Provider<List<ModuleDefinition>> {
    return packageGenerationTask.map { packageTask ->
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
}