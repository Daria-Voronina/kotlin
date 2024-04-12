/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.sir.providers.impl

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.sir.SirEnum
import org.jetbrains.kotlin.sir.SirModule
import org.jetbrains.kotlin.sir.SirMutableDeclarationContainer
import org.jetbrains.kotlin.sir.SirOrigin
import org.jetbrains.kotlin.sir.builder.buildEnum
import org.jetbrains.kotlin.sir.builder.buildModule
import org.jetbrains.kotlin.sir.providers.SirEnumGenerator
import org.jetbrains.kotlin.sir.util.addChild

public class SirEnumGeneratorImpl(
    moduleForPackagesProducer: () -> SirModule
) : SirEnumGenerator {

    public val moduleForStoringPackages: SirModule by lazy { moduleForPackagesProducer() }
    private val createdEnums: MutableMap<FqName, SirEnum> = mutableMapOf()

    override fun FqName.sirPackageEnum(): SirEnum {
        require(!this.isRoot)

        val parent: SirMutableDeclarationContainer = if (parent().isRoot) {
            moduleForStoringPackages
        } else {
            parent().sirPackageEnum()
        }

        return createEnum(this, parent)
    }

    private fun createEnum(fqName: FqName, parent: SirMutableDeclarationContainer): SirEnum = createdEnums.getOrPut(fqName) {
        parent.addChild {
            buildEnum {
                origin = SirOrigin.Namespace(fqName.pathSegments().map { it.asString() })
                name = fqName.pathSegments().last().asString()
            }
        }
    }
}
