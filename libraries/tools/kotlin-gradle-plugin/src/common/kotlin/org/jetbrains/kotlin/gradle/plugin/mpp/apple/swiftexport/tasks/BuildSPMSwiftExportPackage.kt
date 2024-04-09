/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.utils.getFile
import org.jetbrains.kotlin.gradle.utils.listFilesOrEmpty
import org.jetbrains.kotlin.gradle.utils.relativeOrAbsolute
import org.jetbrains.kotlin.gradle.utils.runCommand
import org.jetbrains.kotlin.incremental.createDirectory
import org.jetbrains.kotlin.incremental.deleteRecursivelyOrThrow
import org.jetbrains.kotlin.utils.keysToMap
import java.io.File
import javax.inject.Inject

@DisableCachingByDefault(because = "Swift Export is experimental, so no caching for now")
internal abstract class BuildSPMSwiftExportPackage @Inject constructor(
    private val providerFactory: ProviderFactory,
) : DefaultTask() {

    @get:Input
    abstract val swiftApiModuleName: Property<String>

    @get:Input
    abstract val swiftLibraryName: Property<String>

    @get:Input
    val inheritedBuildSettingsFromEnvironment: Map<String, Provider<String>>
        get() = listOf(
            "CONFIGURATION", "ARCHS", "ONLY_ACTIVE_ARCH",
        ).keysToMap {
            providerFactory.environmentVariable(it)
        }

    @get:Optional
    @get:Input
    val targetDeviceIdentifier: Provider<String>
        get() = providerFactory.environmentVariable("TARGET_DEVICE_IDENTIFIER")

    @get:Input
    val platformName: Provider<String>
        get() = providerFactory.environmentVariable("PLATFORM_NAME")

    @get:Internal
    abstract val packageBuildDirectory: DirectoryProperty

    @get:Internal
    abstract val packageRootDirectory: DirectoryProperty

    @get:Internal
    internal val interfacesPath: File
        get() = packageBuildDirectory.getFile().resolve("dd-interfaces")

    @get:Internal
    val objectFilesPath: File
        get() = packageBuildDirectory.getFile().resolve("dd-o-files")

    @get:Internal
    val libraryFilesPath: File
        get() = packageBuildDirectory.getFile().resolve("dd-a-files")

    @get:Internal
    val buildIntermediatesPath: File
        get() = packageBuildDirectory.getFile().resolve("dd-other")

    @get:Internal
    val packageLibraryPath
        get() = libraryFilesPath.resolve("lib${swiftLibraryName.get()}.a")

    @get:Internal
    val swiftModulePath
        get() = interfacesPath.resolve("${swiftApiModuleName.get()}.swiftmodule")

    private val packageRootDirectoryPath
        get() = packageRootDirectory.getFile()

    @TaskAction
    fun run() {
        buildSyntheticPackage()
        packObjectFilesIntoLibrary()
    }

    private fun buildSyntheticPackage() {
        val intermediatesDestination = mapOf(
            // Thin/universal object files
            "TARGET_BUILD_DIR" to objectFilesPath.canonicalPath,
            // .swiftmodule interface
            "BUILT_PRODUCTS_DIR" to interfacesPath.canonicalPath,
        )
        val inheritedBuildSettings = inheritedBuildSettingsFromEnvironment.mapValues {
            it.value.orNull
        }.filterValues { it != null }

        val command = listOf(
            "xcodebuild",
            "-derivedDataPath", buildIntermediatesPath.relativeOrAbsolute(packageRootDirectoryPath),
            "-scheme", swiftApiModuleName.get(),
            "-destination", destination(),
        ) + (inheritedBuildSettings + intermediatesDestination).map { (k, v) -> "$k=$v" }

        // FIXME: This will not work with dynamic libraries
        runCommand(
            command,
            logger = logger,
            processConfiguration = {
                directory(packageRootDirectoryPath)
            }
        )
    }

    private fun packObjectFilesIntoLibrary() {
        val objectFilePaths = objectFilesPath.listFilesOrEmpty().filter {
            it.extension == "o"
        }.map { it.relativeOrAbsolute(packageRootDirectoryPath) }
        if (objectFilePaths.isEmpty()) {
            error("Synthetic project build didn't produce any object files")
        }

        libraryFilesPath.createDirectory()

        runCommand(
            listOf(
                "libtool", "-static",
                "-o", packageLibraryPath.relativeOrAbsolute(packageRootDirectoryPath),
            ) + objectFilePaths,
            logger = logger,
            processConfiguration = {
                directory(packageRootDirectoryPath)
            }
        )
    }

    private fun destination(): String {
        val deviceId: String? = targetDeviceIdentifier.orNull
        if (deviceId != null) return "id=$deviceId"

        val platformName = platformName.orNull ?: error("Missing a target device identifier and a platform name")
        val platform = mapOf(
            "iphonesimulator" to "iOS Simulator",
            "iphoneos" to "iOS",
            "watchsimulator" to "watchOS Simulator",
            "watchos" to "watchOS",
            "appletvos" to "tvOS",
            "appletvsimulator" to "tvOS Simulator",
            "macosx" to "macOS",
        )[platformName] ?: error("Unknown PLATFORM_NAME $platformName")

        return "generic/platform=$platform"
    }
}