/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.AppleTarget
import org.jetbrains.kotlin.gradle.utils.*
import org.jetbrains.kotlin.gradle.utils.getFile
import org.jetbrains.kotlin.gradle.utils.listFilesOrEmpty
import org.jetbrains.kotlin.gradle.utils.relativeOrAbsolute
import org.jetbrains.kotlin.gradle.utils.runCommand
import org.jetbrains.kotlin.incremental.createDirectory
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import javax.inject.Inject

@DisableCachingByDefault(because = "Swift Export is experimental, so no caching for now")
internal abstract class BuildSPMSwiftExportPackage @Inject constructor(
    providerFactory: ProviderFactory,
    objectsFactory: ObjectFactory,
) : DefaultTask() {

    @get:Input
    abstract val swiftApiModuleName: Property<String>

    @get:Input
    abstract val swiftLibraryName: Property<String>

    @get:Input
    abstract val target: Property<KonanTarget>

    @get:Input
    abstract val configuration: Property<String>

    @get:Input
    val onlyActiveArch: Property<Boolean> = objectsFactory.property(true)

    @get:Optional
    @get:Input
    val targetDeviceIdentifier: Property<String> = objectsFactory.property<String>().convention(
        providerFactory.environmentVariable("TARGET_DEVICE_IDENTIFIER")
    )

    @get:Internal
    abstract val packageDerivedDataDirectory: DirectoryProperty

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

        val buildArguments = mapOf(
            "ONLY_ACTIVE_ARCH" to onlyActiveArch.map { it.appleBool() }.get(),
            "ARCHS" to target.map { it.appleArchitecture() }.get(),
            "CONFIGURATION" to configuration.get(),
        )

        val command = listOf(
            "xcodebuild",
            "-derivedDataPath", packageDerivedDataDirectory.asFile.map { it.relativeOrAbsolute(packageRootDirectoryPath) }.get(),
            "-scheme", swiftApiModuleName.get(),
            "-destination", destination(),
        ) + (intermediatesDestination + buildArguments).map { (k, v) -> "$k=$v" }

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
            error("Synthetic package build didn't produce any object files")
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

        val platformName = target.map { it.applePlatform() }.get() ?: error("Missing platform name")
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

private fun Boolean.appleBool() = if (this) "YES" else "NO"

private fun KonanTarget.appleArchitecture() = when (architecture) {
    Architecture.ARM64 -> "arm64"
    Architecture.ARM32 -> "arm32"
    Architecture.X64 -> "x86_64"
    Architecture.X86 -> throw IllegalArgumentException("Architecture $this is not supported")
}

private fun KonanTarget.applePlatform(): String {
    val appleTarget = AppleTarget.values().first { it.targets.contains(this) }
    return when (appleTarget) {
        AppleTarget.MACOS_DEVICE -> "macOS"
        AppleTarget.IPHONE_DEVICE -> "iphoneos"
        AppleTarget.IPHONE_SIMULATOR -> "iphonesimulator"
        AppleTarget.WATCHOS_DEVICE -> "watchos"
        AppleTarget.WATCHOS_SIMULATOR -> "watchsimulator"
        AppleTarget.TVOS_DEVICE -> "appletvos"
        AppleTarget.TVOS_SIMULATOR -> "appletvsimulator"
    }
}