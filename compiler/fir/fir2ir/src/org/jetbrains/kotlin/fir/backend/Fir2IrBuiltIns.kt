/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend

import org.jetbrains.kotlin.ir.expressions.IrConstructorCall

interface Fir2IrBuiltIns {
    fun enhancedNullabilityAnnotationConstructorCall(): IrConstructorCall?

    fun flexibleNullabilityAnnotationConstructorCall(): IrConstructorCall?

    fun flexibleMutabilityAnnotationConstructorCall(): IrConstructorCall?

    fun flexibleArrayElementVarianceAnnotationConstructorCall(): IrConstructorCall?

    fun rawTypeAnnotationConstructorCall(): IrConstructorCall?
}
