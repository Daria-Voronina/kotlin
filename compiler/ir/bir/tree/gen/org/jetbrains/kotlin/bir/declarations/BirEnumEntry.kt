/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/ir.tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations

import org.jetbrains.kotlin.bir.BirElementBase
import org.jetbrains.kotlin.bir.BirElementVisitor
import org.jetbrains.kotlin.bir.accept
import org.jetbrains.kotlin.bir.expressions.BirExpressionBody
import org.jetbrains.kotlin.bir.symbols.BirEnumEntrySymbol
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI

/**
 * A leaf IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.enumEntry]
 */
abstract class BirEnumEntry : BirElementBase(), BirDeclaration, BirDeclarationWithName,
        BirEnumEntrySymbol {
    @ObsoleteDescriptorBasedAPI
    abstract override val descriptor: ClassDescriptor?

    abstract var initializerExpression: BirExpressionBody?

    abstract var correspondingClass: BirClass?

    override fun <D> acceptChildren(visitor: BirElementVisitor<D>, data: D) {
        initializerExpression?.accept(data, visitor)
        correspondingClass?.accept(data, visitor)
    }

    companion object
}
