/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.internal.SwiftExportConstants
import org.jetbrains.kotlin.gradle.utils.runCommand
import java.io.File
import javax.inject.Inject

@DisableCachingByDefault(because = "This task only copies files")
internal abstract class CopySwiftExportIntermediatesForConsumer @Inject constructor(
    objectFactory: ObjectFactory,
    projectLayout: ProjectLayout,
    providerFactory: ProviderFactory,
    private val fileSystem: FileSystemOperations,
) : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val includeBridgeDirectory: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val includeKotlinRuntimeDirectory: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val builtProductsDirectory: DirectoryProperty = objectFactory.directoryProperty().convention(
        projectLayout.dir(providerFactory.environmentVariable("BUILT_PRODUCTS_DIR").map {
            File(it)
        })
    )

    private val libraries = mutableListOf<Provider<File>>()
    private val interfaces = mutableListOf<Provider<File>>()

    fun addLibrary(library: Provider<File>) {
        libraries.add(library)
    }

    fun addInterface(swiftInterface: Provider<File>) {
        interfaces.add(swiftInterface)
    }

    private val syntheticInterfacesDestinationPath: DirectoryProperty = objectFactory.directoryProperty().convention(
        builtProductsDirectory.flatMap {
            projectLayout.dir(providerFactory.provider {
                it.asFile.resolve(SwiftExportConstants.KOTLIN_BRIDGE)
            })
        }
    )

    private val kotlinRuntimeDestinationPath: DirectoryProperty = objectFactory.directoryProperty().convention(
        builtProductsDirectory.flatMap {
            projectLayout.dir(providerFactory.provider {
                it.asFile.resolve(SwiftExportConstants.KOTLIN_RUNTIME)
            })
        }
    )

    @TaskAction
    fun copy() {
        mergeAndCopyLibrary()
        copyInterfaces()
        copyOtherIncludes()
    }

    private fun mergeAndCopyLibrary() {
        if (libraries.count() > 1) {
            val libsInput = libraries.map { it.get() }
            val output = builtProductsDirectory.map { it.asFile.resolve(libsInput.last().name) }.get().absolutePath

            runCommand(
                listOf(
                    "lipo",
                    "-create",
                    "-output", output,
                ) + libsInput.map { it.absolutePath },
                logger = logger
            )
        } else {
            fileSystem.copy {
                it.from(libraries.single())
                it.into(builtProductsDirectory)
            }
        }
    }

    private fun copyInterfaces() {
        interfaces.forEach { swiftInterface ->
            fileSystem.copy {
                it.from(swiftInterface)
                it.into(builtProductsDirectory)
                it.includeEmptyDirs = false
            }
        }
    }

    private fun copyOtherIncludes() {
        fileSystem.copy {
            it.from(includeBridgeDirectory)
            it.into(syntheticInterfacesDestinationPath)
        }

        fileSystem.copy {
            it.from(includeKotlinRuntimeDirectory)
            it.into(kotlinRuntimeDestinationPath)
        }
    }
}