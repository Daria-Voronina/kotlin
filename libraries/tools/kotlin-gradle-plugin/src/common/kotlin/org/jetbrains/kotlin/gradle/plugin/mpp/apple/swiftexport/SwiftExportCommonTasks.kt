/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformSourceSetConventionsImpl.commonMain
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeBinaryContainer
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractNativeLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.internal.SwiftExportConstants
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.internal.maybeCreateSwiftExportClasspathResolvableConfiguration
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.BuildSPMSwiftExportPackage
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.GenerateSPMPackageFromSwiftExport
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks.SwiftExportTask
import org.jetbrains.kotlin.gradle.tasks.dependsOn
import org.jetbrains.kotlin.gradle.tasks.locateOrRegisterTask
import org.jetbrains.kotlin.gradle.utils.*
import org.jetbrains.kotlin.konan.target.Distribution

internal fun Project.setupCommonSwiftExportPipeline(
    swiftApiModuleName: Provider<String>,
    taskNamePrefix: String,
    target: KotlinNativeTarget,
    buildType: NativeBuildType,
    configuration: (
        packageBuild: TaskProvider<BuildSPMSwiftExportPackage>,
        packageGenerationTask: TaskProvider<GenerateSPMPackageFromSwiftExport>,
        staticLibrary: AbstractNativeLibrary,
    ) -> TaskProvider<out Task>,
): TaskProvider<out Task> {
    val mainCompilation = target.compilations.getByName("main")

    val swiftExportTask = registerSwiftExportRun(
        swiftApiModuleName = swiftApiModuleName
    )
    val staticLibrary = registerSwiftExportCompilationAndGetBinary(
        buildType = buildType,
        compilations = target.compilations,
        binaries = target.binaries,
        mainCompilation = mainCompilation,
        swiftExportTask = swiftExportTask,
    )

    val kotlinStaticLibraryName = staticLibrary.linkTaskProvider.flatMap { it.binary.baseNameProvider }
    val swiftApiLibraryName = swiftApiModuleName.map { it + "Library" }

    val packageGenerationTask = registerPackageGeneration(
        swiftApiModuleName = swiftApiModuleName,
        swiftApiLibraryName = swiftApiLibraryName,
        kotlinStaticLibraryName = kotlinStaticLibraryName,
        swiftExportTask = swiftExportTask,
    )
    val packageBuild = registerSPMPackageBuild(
        taskNamePrefix = taskNamePrefix,
        swiftApiModuleName = swiftApiModuleName,
        swiftApiLibraryName = swiftApiLibraryName,
        staticLibrary = staticLibrary,
        packageGenerationTask = packageGenerationTask,
    )

    return configuration(packageBuild, packageGenerationTask, staticLibrary)
}

private fun Project.registerSwiftExportRun(
    swiftApiModuleName: Provider<String>,
): TaskProvider<SwiftExportTask> {
    val swiftExportTaskName = "swiftExport"

    return locateOrRegisterTask<SwiftExportTask>(swiftExportTaskName) { task ->
        val commonMainProvider = project.future {
            project
                .multiplatformExtension
                .awaitSourceSets()
                .commonMain
                .get()
                .kotlin
                .srcDirs
                .single()
        }

        val outputs = layout.buildDirectory.dir(swiftExportTaskName)
        val swiftIntermediates = outputs.map { it.dir("swiftIntermediates") }
        val kotlinIntermediates = outputs.map { it.dir("kotlinIntermediates") }

        // Input
        task.swiftExportClasspath.from(maybeCreateSwiftExportClasspathResolvableConfiguration())
        task.parameters.sourceRoot.set(commonMainProvider.map { objects.directoryProperty(it) }.getOrThrow())
        task.parameters.swiftApiModuleName.set(swiftApiModuleName)
        task.parameters.bridgeModuleName.set(swiftApiModuleName.map { "${it}Bridge" })
        task.parameters.debugMode.set(true)
        task.parameters.konanDistribution.set(Distribution(konanDistribution.root.absolutePath))

        // Output
        task.parameters.swiftApiPath.set(swiftIntermediates.map { it.file(SwiftExportConstants.KOTLIN_API_SWIFT) })
        task.parameters.headerBridgePath.set(swiftIntermediates.map { it.file(SwiftExportConstants.KOTLIN_BRIDGE_H) })
        task.parameters.kotlinBridgePath.set(kotlinIntermediates.map { it.file(SwiftExportConstants.KOTLIN_BRIDGE_KT) })
    }
}

private fun registerSwiftExportCompilationAndGetBinary(
    buildType: NativeBuildType,
    compilations: NamedDomainObjectContainer<KotlinNativeCompilation>,
    binaries: KotlinNativeBinaryContainer,
    mainCompilation: KotlinCompilation<*>,
    swiftExportTask: TaskProvider<SwiftExportTask>,
): AbstractNativeLibrary {
    val swiftExportCompilationName = "swiftExportMain"
    val swiftExportBinary = "swiftExportBinary"

    compilations.getOrCreate(
        swiftExportCompilationName,
        invokeWhenCreated = { swiftExportCompilation ->
            swiftExportCompilation.associateWith(mainCompilation)
            swiftExportCompilation.defaultSourceSet.kotlin.srcDir(swiftExportTask.map {
                it.parameters.kotlinBridgePath.getFile().parent
            })

            swiftExportCompilation.compileTaskProvider.configure {
                it.compilerOptions.optIn.add("kotlin.experimental.ExperimentalNativeApi")
            }

            binaries.staticLib(swiftExportBinary) { staticLib ->
                staticLib.compilation = swiftExportCompilation
            }
        }
    )

    return binaries.getStaticLib(
        swiftExportBinary,
        buildType
    )
}

private fun Project.registerPackageGeneration(
    swiftApiModuleName: Provider<String>,
    swiftApiLibraryName: Provider<String>,
    kotlinStaticLibraryName: Provider<String>,
    swiftExportTask: TaskProvider<SwiftExportTask>,
): TaskProvider<GenerateSPMPackageFromSwiftExport> {
    val spmPackageGenTaskName = "generateSPMPackage"
    val packageGenerationTask = locateOrRegisterTask<GenerateSPMPackageFromSwiftExport>(spmPackageGenTaskName) { task ->
        task.group = BasePlugin.BUILD_GROUP
        task.description = "Generates SPM Package"

        // Input
        task.kotlinRuntime.set(
            objects.directoryProperty(
                file(Distribution(konanDistribution.root.absolutePath).kotlinRuntimeForSwiftHome)
            )
        )

        task.swiftApiPath.set(swiftExportTask.flatMap { it.parameters.swiftApiPath })
        task.headerBridgePath.set(swiftExportTask.flatMap { it.parameters.headerBridgePath })
        task.headerBridgeModuleName.set(swiftExportTask.flatMap { it.parameters.bridgeModuleName })
        task.swiftLibraryName.set(swiftApiLibraryName)
        task.kotlinLibraryName.set(kotlinStaticLibraryName)
        task.swiftApiModuleName.set(swiftApiModuleName)

        // Output
        task.packagePath.set(layout.buildDirectory.dir("SPMPackage"))
    }

    return packageGenerationTask
}

private fun Project.registerSPMPackageBuild(
    taskNamePrefix: String,
    swiftApiModuleName: Provider<String>,
    swiftApiLibraryName: Provider<String>,
    staticLibrary: AbstractNativeLibrary,
    packageGenerationTask: TaskProvider<GenerateSPMPackageFromSwiftExport>,
): TaskProvider<BuildSPMSwiftExportPackage> {
    val buildTaskName = taskNamePrefix + "BuildSPMPackage"
    val packageBuild = locateOrRegisterTask<BuildSPMSwiftExportPackage>(buildTaskName) { task ->
        task.group = BasePlugin.BUILD_GROUP
        task.description = "Builds $taskNamePrefix SPM package"

        // Input
        task.swiftApiModuleName.set(swiftApiModuleName)
        task.swiftLibraryName.set(swiftApiLibraryName)
        task.packageBuildDirectory.set(layout.buildDirectory.dir("${taskNamePrefix}SPMBuild"))
        task.packageRootDirectory.set(packageGenerationTask.flatMap { it.packagePath })
    }
    packageBuild.dependsOn(staticLibrary.linkTaskProvider)
    packageBuild.dependsOn(packageGenerationTask)
    return packageBuild
}