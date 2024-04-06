/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/ir.tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

@file:Suppress("DuplicatedCode")

package org.jetbrains.kotlin.ir.expressions.impl

import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.IrElementConstructorIndicator

class IrCallImpl internal constructor(
    @Suppress("UNUSED_PARAMETER")
    constructorIndicator: IrElementConstructorIndicator?,
    startOffset: Int,
    endOffset: Int,
    type: IrType,
    origin: IrStatementOrigin?,
    valueArguments: Array<IrExpression?>,
    typeArguments: Array<IrType?>,
    symbol: IrSimpleFunctionSymbol,
    superQualifierSymbol: IrClassSymbol?,
) : IrCall(
    startOffset = startOffset,
    endOffset = endOffset,
    type = type,
    origin = origin,
    valueArguments = valueArguments,
    typeArguments = typeArguments,
    symbol = symbol,
    superQualifierSymbol = superQualifierSymbol,
) {

    companion object {
        // Temporary API for compatible-compose, to be removed soon.
        // Note: It cannot be marked with @Deprecated, because some usages in kotlin compiler pick this declaration up while it still exists.
        fun fromSymbolOwner(
            startOffset: Int,
            endOffset: Int,
            type: IrType,
            symbol: IrSimpleFunctionSymbol,
            typeArgumentsCount: Int = symbol.owner.typeParameters.size,
            valueArgumentsCount: Int = symbol.owner.valueParameters.size,
            origin: IrStatementOrigin? = null,
            superQualifierSymbol: IrClassSymbol? = null,
        ) =
            IrCallImpl(startOffset, endOffset, type, symbol, typeArgumentsCount, valueArgumentsCount, origin, superQualifierSymbol)
    }
}
