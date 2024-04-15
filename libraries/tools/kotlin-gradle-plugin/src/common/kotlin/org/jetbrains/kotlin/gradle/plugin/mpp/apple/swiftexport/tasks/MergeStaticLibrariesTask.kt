/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftexport.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.kotlin.gradle.utils.getFile
import org.jetbrains.kotlin.gradle.utils.runCommand
import org.jetbrains.kotlin.konan.target.HostManager

@DisableCachingByDefault
internal abstract class MergeStaticLibrariesTask : DefaultTask() {
    init {
        onlyIf { HostManager.hostIsMac }
    }

    @get:SkipWhenEmpty
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val libraries: ConfigurableFileCollection

    @get:OutputFile
    abstract val library: RegularFileProperty

    @TaskAction
    fun mergeLibraries() {
        val inputLibs = libraries.asFileTree.map { it.absolutePath }
        val outputLib = library.getFile().absolutePath

        runCommand(
            listOf(
                "libtool",
                "-static",
                "-o", outputLib
            ) + inputLibs,
            logger = logger
        )
    }
}