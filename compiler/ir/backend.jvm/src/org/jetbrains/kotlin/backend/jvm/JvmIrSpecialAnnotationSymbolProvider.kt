/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm

import org.jetbrains.kotlin.backend.common.IrSpecialAnnotationsProvider
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.declarations.addConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.impl.DescriptorlessExternalPackageFragmentSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.createImplicitParameterDeclarationWithWrappedDescriptor
import org.jetbrains.kotlin.load.java.JvmAnnotationNames
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.StandardClassIds.Annotations.EnhancedNullability
import org.jetbrains.kotlin.name.StandardClassIds.Annotations.FlexibleArrayElementVariance
import org.jetbrains.kotlin.name.StandardClassIds.Annotations.FlexibleMutability
import org.jetbrains.kotlin.name.StandardClassIds.Annotations.FlexibleNullability
import org.jetbrains.kotlin.name.StandardClassIds.Annotations.RawTypeAnnotation
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance
import kotlin.apply

class JvmIrSpecialAnnotationSymbolProvider(private val irFactory: IrFactory) : IrSpecialAnnotationsProvider {
    private val kotlinJvmInternalPackage: IrExternalPackageFragmentImpl =
        IrExternalPackageFragmentImpl(DescriptorlessExternalPackageFragmentSymbol(), JvmAnnotationNames.KOTLIN_JVM_INTERNAL)

    private val kotlinInternalIrPackage: IrExternalPackageFragmentImpl =
        IrExternalPackageFragmentImpl(DescriptorlessExternalPackageFragmentSymbol(), IrBuiltIns.KOTLIN_INTERNAL_IR_FQN)

    override val enhancedNullabilityAnnotationCall: IrConstructorCallImpl = EnhancedNullability.toConstructorCall(kotlinJvmInternalPackage)
    override val flexibleNullabilityAnnotationCall: IrConstructorCallImpl = FlexibleNullability.toConstructorCall()
    override val flexibleMutabilityAnnotationCall: IrConstructorCallImpl = FlexibleMutability.toConstructorCall()
    override val rawTypeAnnotationCall: IrConstructorCallImpl = RawTypeAnnotation.toConstructorCall()
    override val flexibleArrayElementVarianceAnnotationCall: IrConstructorCallImpl = FlexibleArrayElementVariance.toConstructorCall()

    private fun ClassId.toConstructorCall(irPackage: IrExternalPackageFragmentImpl = kotlinInternalIrPackage): IrConstructorCallImpl {
        val irClassSymbol = toIrClass(irPackage).symbol
        val constructorSymbol = irClassSymbol.owner.declarations.firstIsInstance<IrConstructor>().symbol
        return IrConstructorCallImpl.fromSymbolOwner(irClassSymbol.defaultType, constructorSymbol)
    }

    private fun ClassId.toIrClass(parent: IrDeclarationParent): IrClass =
        irFactory.buildClass {
            kind = ClassKind.ANNOTATION_CLASS
            name = shortClassName
        }.apply {
            createImplicitParameterDeclarationWithWrappedDescriptor()
            this.parent = parent
            addConstructor {
                isPrimary = true
            }
        }
}
