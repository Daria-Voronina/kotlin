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
import org.jetbrains.kotlin.gradle.utils.getFile
import org.jetbrains.kotlin.gradle.utils.relativeOrAbsolute
import org.jetbrains.kotlin.gradle.utils.runCommand
import org.jetbrains.kotlin.incremental.createDirectory
import org.jetbrains.kotlin.incremental.deleteRecursivelyOrThrow
import org.jetbrains.kotlin.konan.target.HostManager
import java.io.File
import java.io.Serializable
import javax.inject.Inject

internal data class ModuleDefinition(val name: String, val header: File) : Serializable

@DisableCachingByDefault
internal abstract class SwiftExportFrameworkTask @Inject constructor(
    private val fileSystem: FileSystemOperations,
) : DefaultTask() {
    init {
        onlyIf { HostManager.hostIsMac }
    }

    @get:SkipWhenEmpty
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val libraries: ConfigurableFileCollection

    @get:Input
    abstract val binaryName: Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val swiftModule: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val headerDefinitions: ListProperty<ModuleDefinition>

    @get:Internal
    val libraryName: Provider<String>
        get() = binaryName.map { "lib${it}.a" }

    @get:Internal
    val frameworkName: Provider<String>
        get() = binaryName.map { "${it}.xcframework" }

    @get:OutputDirectory
    abstract val frameworkRoot: DirectoryProperty

    private val frameworkRootPath get() = frameworkRoot.getFile()

    private val frameworkPath: File
        get() = frameworkRootPath.resolve(frameworkName.get())

    private val libraryPath: File
        get() = frameworkRootPath.resolve(libraryName.get())

    private val headersPath: File
        get() = frameworkRootPath.resolve("Headers")

    @TaskAction
    fun assembleFramework() {
        prepareFrameworkDirectory()
        assembleBinary()
        prepareHeaders()
        createXCFramework()
        copyModule()
        cleanup()
    }

    private fun prepareFrameworkDirectory() {
        if (frameworkRootPath.exists()) {
            frameworkRootPath.deleteRecursivelyOrThrow()
        }
        headersPath.createDirectory()
    }

    private fun assembleBinary() {
        if (libraries.asFileTree.count() <= 1) {
            return
        }

        runCommand(
            listOf(
                "libtool",
                "-static",
                "-o", libraryPath.name
            ) + libraries.asFileTree.map { it.relativeOrAbsolute(frameworkRootPath) },
            logger = logger,
            processConfiguration = {
                directory(frameworkRootPath)
            }
        )
    }

    private fun createXCFramework() {
        runCommand(
            listOf(
                "xcodebuild",
                "-create-xcframework",
                "-library", libraryPath.name,
                "-headers", headersPath.relativeOrAbsolute(frameworkRootPath),
                "-allow-internal-distribution",
                "-output", binaryName.map { "$it.xcframework" }.get()
            ),
            logger = logger,
            processConfiguration = {
                directory(frameworkRootPath)
            }
        )
    }

    private fun prepareHeaders() {
        val modulemap = headersPath.resolve("module.modulemap")
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
                it.into(headersPath)
            }
        }
    }

    private fun copyModule() {
        frameworkPath.listFiles()?.let { arch ->
            arch.filter {
                it.isDirectory
            }.forEach { targetFramework ->
                fileSystem.copy {
                    it.from(swiftModule)
                    it.into(targetFramework.resolve(swiftModule.getFile().name))
                }
            }
        }
    }

    private fun cleanup() {
        fileSystem.delete {
            it.delete(libraryPath)
        }
        fileSystem.delete {
            it.delete(headersPath)
        }
    }
}