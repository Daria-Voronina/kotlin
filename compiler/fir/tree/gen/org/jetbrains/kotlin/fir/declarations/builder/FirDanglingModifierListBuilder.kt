/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode", "unused")

package org.jetbrains.kotlin.fir.declarations.builder

import kotlin.contracts.*
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.builder.FirAnnotationContainerBuilder
import org.jetbrains.kotlin.fir.builder.FirBuilderDsl
import org.jetbrains.kotlin.fir.builder.toMutableOrEmpty
import org.jetbrains.kotlin.fir.declarations.FirDanglingModifierList
import org.jetbrains.kotlin.fir.declarations.FirDeclarationAttributes
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.FirResolveState
import org.jetbrains.kotlin.fir.declarations.ResolveStateAccess
import org.jetbrains.kotlin.fir.declarations.asResolveState
import org.jetbrains.kotlin.fir.declarations.impl.FirDanglingModifierListImpl
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.symbols.impl.FirDanglingModifierSymbol
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically by [org.jetbrains.kotlin.fir.tree.generator.MainKt]
 * DO NOT MODIFY IT MANUALLY
 */

@FirBuilderDsl
class FirDanglingModifierListBuilder : FirAnnotationContainerBuilder {
    override var source: KtSourceElement? = null
    var resolvePhase: FirResolvePhase = FirResolvePhase.RAW_FIR
    override val annotations: MutableList<FirAnnotation> = mutableListOf()
    lateinit var moduleData: FirModuleData
    lateinit var origin: FirDeclarationOrigin
    var attributes: FirDeclarationAttributes = FirDeclarationAttributes()
    lateinit var diagnostic: ConeDiagnostic
    lateinit var symbol: FirDanglingModifierSymbol

    override fun build(): FirDanglingModifierList {
        return FirDanglingModifierListImpl(
            source,
            resolvePhase,
            annotations.toMutableOrEmpty(),
            moduleData,
            origin,
            attributes,
            diagnostic,
            symbol,
        )
    }

}

@OptIn(ExperimentalContracts::class)
inline fun buildDanglingModifierList(init: FirDanglingModifierListBuilder.() -> Unit): FirDanglingModifierList {
    contract {
        callsInPlace(init, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return FirDanglingModifierListBuilder().apply(init).build()
}
