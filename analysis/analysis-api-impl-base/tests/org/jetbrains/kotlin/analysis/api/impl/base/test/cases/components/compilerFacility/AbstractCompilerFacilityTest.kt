/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.compilerFacility

import com.intellij.openapi.extensions.LoadingOrder
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.components.KtCompilationResult
import org.jetbrains.kotlin.analysis.api.components.KtCompiledFile
import org.jetbrains.kotlin.analysis.api.components.KtCompilerFacility
import org.jetbrains.kotlin.analysis.api.components.KtCompilerTarget
import org.jetbrains.kotlin.analysis.api.diagnostics.KtDiagnostic
import org.jetbrains.kotlin.analysis.api.diagnostics.KtDiagnosticWithPsi
import org.jetbrains.kotlin.analysis.test.framework.base.AbstractAnalysisApiBasedTest
import org.jetbrains.kotlin.analysis.test.framework.services.expressionMarkerProvider
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.parentClassId
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot
import org.jetbrains.kotlin.codegen.BytecodeListingTextCollectingVisitor
import org.jetbrains.kotlin.codegen.ClassBuilderFactories
import org.jetbrains.kotlin.codegen.forTestCompile.ForTestCompileRuntime
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.plugin.services.PluginRuntimeAnnotationsProvider
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrFieldAccessExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.DumpIrTreeOptions
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCodeFragment
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.test.Constructor
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.directives.model.DirectiveApplicability
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.RuntimeClasspathProvider
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.ClassNode
import java.io.File
import kotlin.test.assertFalse

abstract class AbstractMultiModuleCompilerFacilityTest : AbstractCompilerFacilityTest()

abstract class AbstractFirPluginPrototypeMultiModuleCompilerFacilityTest : AbstractCompilerFacilityTest() {
    override fun extraCustomRuntimeClasspathProviders(): Array<Constructor<RuntimeClasspathProvider>> =
        arrayOf(::PluginRuntimeAnnotationsProvider)
}

abstract class AbstractCompilerFacilityTest : AbstractAnalysisApiBasedTest() {
    private companion object {
        private val ALLOWED_ERRORS = listOf(
            FirErrors.INVISIBLE_REFERENCE,
            FirErrors.INVISIBLE_SETTER,
            FirErrors.DEPRECATION_ERROR,
            FirErrors.DIVISION_BY_ZERO,
            FirErrors.OPT_IN_USAGE_ERROR,
            FirErrors.OPT_IN_OVERRIDE_ERROR,
            FirErrors.UNSAFE_CALL,
            FirErrors.UNSAFE_IMPLICIT_INVOKE_CALL,
            FirErrors.UNSAFE_INFIX_CALL,
            FirErrors.UNSAFE_OPERATOR_CALL,
            FirErrors.ITERATOR_ON_NULLABLE,
            FirErrors.UNEXPECTED_SAFE_CALL,
            FirErrors.DSL_SCOPE_VIOLATION,
        ).map { it.name }
    }

    override fun doTestByMainFile(mainFile: KtFile, mainModule: TestModule, testServices: TestServices) {
        val testFile = mainModule.files.single { it.name == mainFile.name }

        val checkComposableFunctions = mainModule.directives.contains(Directives.CHECK_COMPOSABLE_CALL)
        val irCollector = CollectingIrGenerationExtension(checkComposableFunctions)

        val project = mainFile.project
        project.extensionArea.getExtensionPoint(IrGenerationExtension.extensionPointName)
            .registerExtension(irCollector, LoadingOrder.LAST, project)

        val compilerConfiguration = CompilerConfiguration().apply {
            put(CommonConfigurationKeys.MODULE_NAME, mainModule.name)
            put(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS, mainModule.languageVersionSettings)
            put(JVMConfigurationKeys.IR, true)

            testFile.directives[Directives.CODE_FRAGMENT_CLASS_NAME].singleOrNull()
                ?.let { put(KtCompilerFacility.CODE_FRAGMENT_CLASS_NAME, it) }

            testFile.directives[Directives.CODE_FRAGMENT_METHOD_NAME].singleOrNull()
                ?.let { put(KtCompilerFacility.CODE_FRAGMENT_METHOD_NAME, it) }
        }

        analyze(mainFile) {
            val target = KtCompilerTarget.Jvm(ClassBuilderFactories.TEST)
            val allowedErrorFilter: (KtDiagnostic) -> Boolean = { it.factoryName in ALLOWED_ERRORS }

            val result = compile(mainFile, compilerConfiguration, target, allowedErrorFilter)

            val actualText = when (result) {
                is KtCompilationResult.Failure -> result.errors.joinToString("\n") { dumpDiagnostic(it) }
                is KtCompilationResult.Success -> dumpClassFiles(result.output)
            }

            testServices.assertions.assertEqualsToTestDataFileSibling(actualText)

            if (result is KtCompilationResult.Success) {
                testServices.assertions.assertEqualsToTestDataFileSibling(irCollector.result, extension = ".ir.txt")
            }

            if (checkComposableFunctions) {
                testServices.assertions.assertEqualsToTestDataFileSibling(
                    irCollector.composableFunctions.joinToString("\n"), extension = ".composable.txt"
                )
            }
        }
    }

    open fun extraCustomRuntimeClasspathProviders(): Array<Constructor<RuntimeClasspathProvider>> = emptyArray()

    override fun configureTest(builder: TestConfigurationBuilder) {
        super.configureTest(builder)
        with(builder) {
            useDirectives(Directives)
            useConfigurators(::CompilerFacilityEnvironmentConfigurator)
            defaultDirectives {
                +ConfigurationDirectives.WITH_STDLIB
                +JvmEnvironmentConfigurationDirectives.FULL_JDK
            }
            useCustomRuntimeClasspathProviders(*extraCustomRuntimeClasspathProviders())
        }
    }

    private fun dumpDiagnostic(diagnostic: KtDiagnostic): String {
        val textRanges = when (diagnostic) {
            is KtDiagnosticWithPsi<*> -> {
                diagnostic.textRanges.singleOrNull()?.toString()
                    ?: diagnostic.textRanges.joinToString(prefix = "[", postfix = "]")
            }
            else -> null
        }

        return buildString {
            if (textRanges != null) {
                append(textRanges)
                append(" ")
            }
            append(diagnostic.factoryName)
            append(" ")
            append(diagnostic.defaultMessage)
        }
    }

    private fun dumpClassFiles(outputFiles: List<KtCompiledFile>): String {
        val classes = outputFiles
            .filter { it.path.endsWith(".class", ignoreCase = true) }
            .also { check(it.isNotEmpty()) }
            .sortedBy { it.path }
            .map { outputFile ->
                val classReader = ClassReader(outputFile.content)
                ClassNode(Opcodes.API_VERSION).also { classReader.accept(it, ClassReader.SKIP_CODE) }
            }

        val allClasses = classes.associateBy { Type.getObjectType(it.name) }

        return classes.joinToString("\n\n") { node ->
            val visitor = BytecodeListingTextCollectingVisitor(
                BytecodeListingTextCollectingVisitor.Filter.EMPTY,
                allClasses,
                withSignatures = false,
                withAnnotations = false,
                sortDeclarations = true
            )

            node.accept(visitor)
            visitor.text
        }
    }

    object Directives : SimpleDirectivesContainer() {
        val CODE_FRAGMENT_CLASS_NAME by stringDirective(
            "Short name of a code fragment class",
            applicability = DirectiveApplicability.File
        )

        val CODE_FRAGMENT_METHOD_NAME by stringDirective(
            "Name of a code fragment facade method",
            applicability = DirectiveApplicability.File
        )

        val ATTACH_DUPLICATE_STDLIB by directive(
            "Attach the 'stdlib-jvm-minimal-for-test' library to simulate duplicate stdlib dependency"
        )

        val CHECK_COMPOSABLE_CALL by directive(
            "Check whether all functions of calls and getters of properties with MyComposable annotation are listed in *.composable.txt or not"
        )
    }
}

private class CompilerFacilityEnvironmentConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {
    override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
        if (module.directives.contains(AbstractCompilerFacilityTest.Directives.ATTACH_DUPLICATE_STDLIB)) {
            configuration.add(CLIConfigurationKeys.CONTENT_ROOTS, JvmClasspathRoot(ForTestCompileRuntime.minimalRuntimeJarForTests()))
        }
    }
}

internal fun createCodeFragment(ktFile: KtFile, module: TestModule, testServices: TestServices): KtCodeFragment? {
    val ioFile = module.files.single { it.name == ktFile.name }.originalFile
    val ioFragmentFile = File(ioFile.parent, "${ioFile.nameWithoutExtension}.fragment.${ioFile.extension}")

    if (!ioFragmentFile.exists()) {
        return null
    }

    val contextElement = testServices.expressionMarkerProvider.getElementOfTypeAtCaret<KtElement>(ktFile)

    val fragmentText = ioFragmentFile.readText()
    val isBlockFragment = fragmentText.any { it == '\n' }

    val project = ktFile.project
    val factory = KtPsiFactory(project, markGenerated = false)

    return when {
        isBlockFragment -> factory.createBlockCodeFragment(fragmentText, contextElement)
        else -> factory.createExpressionCodeFragment(fragmentText, contextElement)
    }
}

private class CollectingIrGenerationExtension(private val collectComposableFunctions: Boolean) : IrGenerationExtension {
    lateinit var result: String
        private set

    val composableFunctions: MutableSet<String> = mutableSetOf()

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        assertFalse { ::result.isInitialized }

        val dumpOptions = DumpIrTreeOptions(
            normalizeNames = true,
            stableOrder = true,
            printModuleName = false,
            printFilePath = false
        )

        result = moduleFragment.dump(dumpOptions)

        if (collectComposableFunctions) {
            moduleFragment.accept(ComposableCallVisitor { composableFunctions.add(it.name.asString()) }, null)
        }
    }

    /**
     * This class recursively visits all calls of functions and getters, and if the function or the getter used for a call has
     * `MyComposable` annotation, it runs [handleComposable] for the function or the getter.
     */
    private class ComposableCallVisitor(private val handleComposable: (declaration: IrDeclarationWithName) -> Unit) : IrElementVisitorVoid {
        val MyComposableClassId = ClassId(FqName("org.jetbrains.kotlin.fir.plugin"), FqName("MyComposable"), false)

        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }

        override fun visitCall(expression: IrCall) {
            @OptIn(UnsafeDuringIrConstructionAPI::class)
            val function = expression.symbol.owner
            if (function.containsComposableAnnotation()) {
                handleComposable(function)
            }
        }

        override fun visitFieldAccess(expression: IrFieldAccessExpression) {
            @OptIn(UnsafeDuringIrConstructionAPI::class)
            val field = expression.symbol.owner
            if (field.containsComposableAnnotation()) {
                handleComposable(field)
            }
        }

        private fun IrAnnotationContainer.containsComposableAnnotation() =
            @OptIn(UnsafeDuringIrConstructionAPI::class)
            annotations.any { it.symbol.owner.parentClassId == MyComposableClassId }
    }
}