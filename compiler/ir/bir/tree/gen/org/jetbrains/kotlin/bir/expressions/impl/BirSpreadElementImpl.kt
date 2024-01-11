/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir.tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.expressions.impl

import org.jetbrains.kotlin.bir.BirElement
import org.jetbrains.kotlin.bir.BirElementVisitorLite
import org.jetbrains.kotlin.bir.CompressedSourceSpan
import org.jetbrains.kotlin.bir.acceptLite
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.expressions.BirSpreadElement

class BirSpreadElementImpl(
    sourceSpan: CompressedSourceSpan,
    expression: BirExpression?,
) : BirSpreadElement(BirSpreadElement) {
    private var _sourceSpan: CompressedSourceSpan = sourceSpan
    /**
     * The span of source code of the syntax node from which this BIR node was generated,
     * in number of characters from the start the source file. If there is no source information for this BIR node,
     * the [SourceSpan.UNDEFINED] is used. In order to get the line number and the column number from this offset,
     * [IrFileEntry.getLineNumber] and [IrFileEntry.getColumnNumber] can be used.
     *
     * @see IrFileEntry.getSourceRangeInfo
     */
    override var sourceSpan: CompressedSourceSpan
        get() {
            recordPropertyRead(2)
            return _sourceSpan
        }
        set(value) {
            if (_sourceSpan != value) {
                _sourceSpan = value
                invalidate(2)
            }
        }

    private var _expression: BirExpression? = expression
    override var expression: BirExpression?
        get() {
            recordPropertyRead(1)
            return _expression
        }
        set(value) {
            if (_expression !== value) {
                childReplaced(_expression, value)
                _expression = value
                invalidate(1)
            }
        }


    init {
        initChild(_expression)
    }

    override fun acceptChildrenLite(visitor: BirElementVisitorLite) {
        _expression?.acceptLite(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?): Int {
        return when {
            this._expression === old -> {
                this._expression = new as BirExpression?
                1
            }
            else -> throwChildForReplacementNotFound(old)
        }
    }
}
