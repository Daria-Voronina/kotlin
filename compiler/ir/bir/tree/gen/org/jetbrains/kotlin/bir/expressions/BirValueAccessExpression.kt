/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir.tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementClass
import org.jetbrains.kotlin.bir.declarations.BirValueDeclaration
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin

abstract class BirValueAccessExpression(elementClass: BirElementClass<*>) : BirDeclarationReference(elementClass), BirElement {
    abstract override var symbol: BirValueDeclaration
    abstract var origin: IrStatementOrigin?

    companion object : BirElementClass<BirValueAccessExpression>(BirValueAccessExpression::class.java, 100, false)
}
