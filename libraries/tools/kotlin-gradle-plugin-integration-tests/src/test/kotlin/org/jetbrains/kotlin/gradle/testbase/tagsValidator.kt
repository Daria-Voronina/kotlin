/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.testbase

import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import org.junit.platform.commons.support.AnnotationSupport.findAnnotation
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * Extension for JUnit 5 tests checking that only one test tag is applied to the test method.
 *
 * Just add it to the test class.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(TagsCountValidatorInterceptor::class)
annotation class TagsCountValidator

class TagsCountValidatorInterceptor : InvocationInterceptor {
    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext,
    ) {
        val testTags = findTestAnnotations(extensionContext.element)

        if (testTags.isEmpty()) {
            invocation.skip()
            throw IllegalStateException("Test method either does not have test tag annotation (for example @JvmGradlePluginTests) or test tag is not known to this validator.")
        } else if (testTags.size > 1) {
            invocation.skip()
            throw IllegalStateException(
                """
                Test method should not have more then one test tag annotated on method and class combined!
                Current test has ${testTags.joinToString()} test tags.
                """.trimIndent()
            )
        } else {
            invocation.proceed()
        }
    }

    private fun findTestAnnotations(element: Optional<AnnotatedElement>): List<Annotation> {
        return listOfNotNull(
            findAnnotation(element, JvmGradlePluginTests::class.java).getOrNull(),
            findAnnotation(element, JsGradlePluginTests::class.java).getOrNull(),
            findAnnotation(element, NativeGradlePluginTests::class.java).getOrNull(),
            findAnnotation(element, MppGradlePluginTests::class.java).getOrNull(),
            findAnnotation(element, AndroidGradlePluginTests::class.java).getOrNull(),
            findAnnotation(element, OtherGradlePluginTests::class.java).getOrNull(),
            findAnnotation(element, SwiftExportGradlePluginTests::class.java).getOrNull(),
        )
    }
}
