/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend.jvm

import org.jetbrains.kotlin.backend.common.IrSpecialAnnotationsProvider
import org.jetbrains.kotlin.backend.jvm.JvmIrSpecialAnnotationSymbolProvider
import org.jetbrains.kotlin.ir.declarations.IrFactory

class JvmIrSpecialAnnotationSymbolProvider(irFactory: IrFactory) :
    IrSpecialAnnotationsProvider by JvmIrSpecialAnnotationSymbolProvider(irFactory)
