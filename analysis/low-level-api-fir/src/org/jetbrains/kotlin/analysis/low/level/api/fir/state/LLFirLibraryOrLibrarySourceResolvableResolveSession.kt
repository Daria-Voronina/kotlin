/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.state

internal class LLFirLibraryOrLibrarySourceResolvableResolveSession(
    moduleProvider: LLModuleProvider,
    moduleKindProvider: LLModuleKindProvider,
    sessionProvider: LLSessionProvider,
    diagnosticProvider: LLDiagnosticProvider
) : LLFirResolvableResolveSession(
    moduleProvider = moduleProvider,
    moduleKindProvider = moduleKindProvider,
    sessionProvider = sessionProvider,
    diagnosticProvider = diagnosticProvider
)