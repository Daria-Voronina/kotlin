/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/ir.tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.ir.declarations

import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

/**
 * Generated from: [org.jetbrains.kotlin.ir.generator.IrTree.errorDeclaration]
 */
abstract class IrErrorDeclaration(
    startOffset: Int,
    endOffset: Int,
    origin: IrDeclarationOrigin,
) : IrDeclarationBase(
    startOffset = startOffset,
    endOffset = endOffset,
    origin = origin,
) {

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R =
        visitor.visitErrorDeclaration(this, data)
}
