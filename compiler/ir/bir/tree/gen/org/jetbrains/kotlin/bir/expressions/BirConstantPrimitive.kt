/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir.tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementClass
import org.jetbrains.kotlin.bir.BirElementVisitor
import org.jetbrains.kotlin.bir.accept

/**
 * A leaf IR tree element.
 *
 * Generated from: [org.jetbrains.kotlin.bir.generator.BirTree.constantPrimitive]
 */
abstract class BirConstantPrimitive(elementClass: BirElementClass<*>) : BirConstantValue(elementClass), BirElement {
    abstract var value: BirConst<*>?

    override fun <D> acceptChildren(visitor: BirElementVisitor<D>, data: D) {
        value?.accept(data, visitor)
    }

    companion object : BirElementClass<BirConstantPrimitive>(BirConstantPrimitive::class.java, 14, true)
}
