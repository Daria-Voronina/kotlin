/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DuplicatedCode", "unused")

package org.jetbrains.kotlin.fir.impl

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirPackageDirective
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically by [org.jetbrains.kotlin.fir.tree.generator.MainKt]
 * DO NOT MODIFY IT MANUALLY
 */

internal class FirPackageDirectiveImpl(
    override val source: KtSourceElement?,
    override val packageFqName: FqName,
) : FirPackageDirective() {
    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {}

    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirPackageDirectiveImpl {
        return this
    }
}
