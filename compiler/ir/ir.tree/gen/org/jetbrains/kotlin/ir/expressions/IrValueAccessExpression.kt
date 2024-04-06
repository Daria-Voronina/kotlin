/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/ir.tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.ir.expressions

import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.IrType

/**
 * Generated from: [org.jetbrains.kotlin.ir.generator.IrTree.valueAccessExpression]
 */
abstract class IrValueAccessExpression(
    startOffset: Int,
    endOffset: Int,
    type: IrType,
    override var symbol: IrValueSymbol,
    var origin: IrStatementOrigin?,
) : IrDeclarationReference(
    startOffset = startOffset,
    endOffset = endOffset,
    type = type,
) {
}
