/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.AppleTarget
import org.jetbrains.kotlin.gradle.utils.getFile
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.gradle.utils.relativeOrAbsolute
import org.jetbrains.kotlin.gradle.utils.runCommand
import org.jetbrains.kotlin.incremental.createDirectory
import org.jetbrains.kotlin.incremental.deleteRecursivelyOrThrow
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.Serializable
import javax.inject.Inject

internal data class HeaderDefinition(
    val name: String,
    val header: File,
) : Serializable

internal data class LibraryDefinition(
    val prefix: String,
    val library: File,
    val swiftModule: File,
) : Serializable

@DisableCachingByDefault
internal abstract class SwiftExportFrameworkTask @Inject constructor(
    private val fileSystem: FileSystemOperations,
) : DefaultTask() {
    init {
        onlyIf { HostManager.hostIsMac }
    }

    @get:Input
    abstract val binaryName: Property<String>

    @get:Input
    @get:Optional
    abstract val headerDefinitions: ListProperty<HeaderDefinition>

    @get:Internal
    val frameworkName: Provider<String>
        get() = binaryName.map { "${it}.xcframework" }

    @get:OutputDirectory
    abstract val frameworkRoot: DirectoryProperty

    private val frameworkRootPath get() = frameworkRoot.getFile()

    private val headersPath: File
        get() = frameworkRootPath.resolve("Headers")

    private val librariesPath: File
        get() = frameworkRootPath.resolve("Libraries")

    private val swiftModulesPath: File
        get() = frameworkRootPath.resolve("Modules")

    private val libraryDefinitions = mutableMapOf<KonanTarget, Provider<LibraryDefinition>>()

    fun library(libraryDefinition: Provider<LibraryDefinition>, target: KonanTarget) {
        libraryDefinitions[target] = libraryDefinition
    }

    @TaskAction
    fun assembleFramework() {
        prepareFrameworkDirectory()

        val libraries = prepareLibraries()
        createXCFramework(libraries)
        moveModules()
        cleanup()
    }

    private fun prepareFrameworkDirectory() {
        if (frameworkRootPath.exists()) {
            frameworkRootPath.deleteRecursivelyOrThrow()
        }

        headersPath.createDirectory()
        librariesPath.createDirectory()
        swiftModulesPath.createDirectory()
    }

    private fun prepareLibraries(): List<LibraryDefinition> {
        val targets = AppleTarget.values().mapNotNull { aTarget ->
            val libs = libraryDefinitions.filterKeys { target ->
                aTarget.targets.contains(target)
            }.values

            mergeDefinitions(
                libs.map { it.get() },
                aTarget
            )
        }

        return targets
    }

    private fun mergeDefinitions(libraries: List<LibraryDefinition>, appleTarget: AppleTarget): LibraryDefinition? {
        if (libraries.isEmpty()) {
            return null
        }

        if (libraries.count() == 1) {
            return libraries.single()
        }

        val outputName = lowerCamelCaseName(
            "lib",
            binaryName.get(),
            ".a"
        )

        val inputLibs = libraries.map { it.library.absolutePath }
        val output = librariesPath.resolve(appleTarget.targetName).apply { createDirectory() }.resolve(outputName)

        val command = listOf(
            "lipo",
            "-create",
            "-output", output.absolutePath,
        ) + inputLibs

        runCommand(
            command,
            logger = logger
        )

        return LibraryDefinition(
            appleTarget.targetName,
            librariesPath.resolve(output),
            mergeSwiftModules(libraries, appleTarget)
        )
    }

    private fun mergeSwiftModules(libraries: List<LibraryDefinition>, appleTarget: AppleTarget): File {
        val moduleName = binaryName.map { "${it}.swiftmodule" }.get()
        val modulePath = swiftModulesPath.resolve("${appleTarget.targetName}/$moduleName").apply {
            createDirectory()
        }

        libraries.forEach { def ->
            fileSystem.copy {
                it.from(def.swiftModule)
                it.into(modulePath)
                it.duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
        }

        return modulePath
    }

    private fun createXCFramework(libraries: List<LibraryDefinition>) {
        val inputLibs = libraries.map { libDef ->
            val headers = prepareHeaders(libDef)

            listOf(
                "-library",
                libDef.library.relativeOrAbsolute(frameworkRootPath),
                "-headers",
                headers.relativeOrAbsolute(frameworkRootPath)
            )
        }.flatten()

        val command = listOf(
            "xcodebuild",
            "-create-xcframework",
            "-allow-internal-distribution",
            "-output", frameworkName.get()
        ) + inputLibs

        runCommand(
            command,
            logger = logger,
            processConfiguration = {
                directory(frameworkRootPath)
            }
        )
    }

    private fun prepareHeaders(libDef: LibraryDefinition): File {
        val libHeaders = headersPath.resolve(libDef.prefix).apply {
            createDirectory()
        }

        val modulemap = libHeaders.resolve("module.modulemap")
        headerDefinitions.getOrElse(emptyList()).forEach { moduleDef ->
            modulemap.appendText(
                """
                |module ${moduleDef.name} {
                |   header "${moduleDef.header.name}"
                |   export *
                |}
                |
                """.trimMargin()
            )

            fileSystem.copy {
                it.from(moduleDef.header)
                it.into(libHeaders)
            }
        }

        fileSystem.copy {
            it.from(libDef.swiftModule.parentFile)
            it.into(libHeaders)
            it.includeEmptyDirs = false
        }

        return libHeaders
    }

    private fun moveModules() {
        val frameworkPath = frameworkRootPath.resolve(frameworkName.get())

        frameworkPath.listFiles()?.let { arch ->
            arch.filter {
                it.isDirectory
            }.forEach { targetFramework ->
                val headers = targetFramework.resolve("Headers")
                val swiftModules = headers.walkTopDown().filter {
                    it.isDirectory && it.name.endsWith(".swiftmodule")
                }

                swiftModules.forEach { swiftModule ->
                    fileSystem.copy {
                        it.from(swiftModule)
                        it.into(targetFramework.resolve(swiftModule.name))
                    }

                    fileSystem.delete {
                        it.delete(swiftModule)
                    }
                }
            }
        }
    }

    private fun cleanup() {
        listOf(librariesPath, headersPath, swiftModulesPath).forEach { path ->
            try {
                fileSystem.delete {
                    it.delete(path)
                }
            } catch (e: Exception) {
                logger.warn("Can't delete ${path.absolutePath} folder", e)
            }
        }
    }
}