/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/ir/bir.tree/tree-generator/ReadMe.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.bir.declarations.impl

import org.jetbrains.kotlin.bir.*
import org.jetbrains.kotlin.bir.declarations.BirVariable
import org.jetbrains.kotlin.bir.expressions.BirConstructorCall
import org.jetbrains.kotlin.bir.expressions.BirExpression
import org.jetbrains.kotlin.bir.types.BirType
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.name.Name

class BirVariableImpl(
    sourceSpan: CompressedSourceSpan,
    signature: IdSignature?,
    origin: IrDeclarationOrigin,
    name: Name,
    type: BirType,
    isAssignable: Boolean,
    isVar: Boolean,
    isConst: Boolean,
    isLateinit: Boolean,
    initializer: BirExpression?,
) : BirVariable(BirVariable) {
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
            recordPropertyRead(10)
            return _sourceSpan
        }
        set(value) {
            if (_sourceSpan != value) {
                _sourceSpan = value
                invalidate(10)
            }
        }

    private var _signature: IdSignature? = signature
    override var signature: IdSignature?
        get() {
            recordPropertyRead(11)
            return _signature
        }
        set(value) {
            if (_signature != value) {
                _signature = value
                invalidate(11)
            }
        }

    private var _origin: IrDeclarationOrigin = origin
    override var origin: IrDeclarationOrigin
        get() {
            recordPropertyRead(3)
            return _origin
        }
        set(value) {
            if (_origin != value) {
                _origin = value
                invalidate(3)
            }
        }

    private var _name: Name = name
    override var name: Name
        get() {
            recordPropertyRead(4)
            return _name
        }
        set(value) {
            if (_name != value) {
                _name = value
                invalidate(4)
            }
        }

    private var _type: BirType = type
    override var type: BirType
        get() {
            recordPropertyRead(5)
            return _type
        }
        set(value) {
            if (_type != value) {
                _type = value
                invalidate(5)
            }
        }

    private var _isAssignable: Boolean = isAssignable
    override var isAssignable: Boolean
        get() {
            recordPropertyRead(6)
            return _isAssignable
        }
        set(value) {
            if (_isAssignable != value) {
                _isAssignable = value
                invalidate(6)
            }
        }

    private var _isVar: Boolean = isVar
    override var isVar: Boolean
        get() {
            recordPropertyRead(7)
            return _isVar
        }
        set(value) {
            if (_isVar != value) {
                _isVar = value
                invalidate(7)
            }
        }

    private var _isConst: Boolean = isConst
    override var isConst: Boolean
        get() {
            recordPropertyRead(8)
            return _isConst
        }
        set(value) {
            if (_isConst != value) {
                _isConst = value
                invalidate(8)
            }
        }

    private var _isLateinit: Boolean = isLateinit
    override var isLateinit: Boolean
        get() {
            recordPropertyRead(9)
            return _isLateinit
        }
        set(value) {
            if (_isLateinit != value) {
                _isLateinit = value
                invalidate(9)
            }
        }

    private var _initializer: BirExpression? = initializer
    override var initializer: BirExpression?
        get() {
            recordPropertyRead(2)
            return _initializer
        }
        set(value) {
            if (_initializer !== value) {
                childReplaced(_initializer, value)
                _initializer = value
                invalidate(2)
            }
        }

    override val annotations: BirImplChildElementList<BirConstructorCall> = BirImplChildElementList(this, 1, false)

    init {
        initChild(_initializer)
    }

    override fun acceptChildrenLite(visitor: BirElementVisitorLite) {
        annotations.acceptChildrenLite(visitor)
        _initializer?.acceptLite(visitor)
    }

    override fun replaceChildProperty(old: BirElement, new: BirElement?): Int {
        return when {
            this._initializer === old -> {
                this._initializer = new as BirExpression?
                2
            }
            else -> throwChildForReplacementNotFound(old)
        }
    }

    override fun getChildrenListById(id: Int): BirChildElementList<*> {
        return when (id) {
            1 -> this.annotations
            else -> throwChildrenListWithIdNotFound(id)
        }
    }
}
