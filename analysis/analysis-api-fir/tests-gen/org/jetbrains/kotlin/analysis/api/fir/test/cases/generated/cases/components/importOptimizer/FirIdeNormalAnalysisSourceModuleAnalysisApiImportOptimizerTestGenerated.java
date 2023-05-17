/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.test.cases.generated.cases.components.importOptimizer;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.analysis.api.fir.test.configurators.AnalysisApiFirTestConfiguratorFactory;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfiguratorFactoryData;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfigurator;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.TestModuleKind;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.FrontendKind;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisSessionMode;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiMode;
import org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.importOptimizer.AbstractAnalysisApiImportOptimizerTest;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.analysis.api.GenerateAnalysisApiTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("analysis/analysis-api/testData/components/importOptimizer/analyseImports")
@TestDataPath("$PROJECT_ROOT")
public class FirIdeNormalAnalysisSourceModuleAnalysisApiImportOptimizerTestGenerated extends AbstractAnalysisApiImportOptimizerTest {
    @NotNull
    @Override
    public AnalysisApiTestConfigurator getConfigurator() {
        return AnalysisApiFirTestConfiguratorFactory.INSTANCE.createConfigurator(
            new AnalysisApiTestConfiguratorFactoryData(
                FrontendKind.Fir,
                TestModuleKind.Source,
                AnalysisSessionMode.Normal,
                AnalysisApiMode.Ide
            )
        );
    }

    @Test
    public void testAllFilesPresentInAnalyseImports() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/importOptimizer/analyseImports"), Pattern.compile("^([^.]+)\\.kt$"), null, true);
    }

    @Test
    @TestMetadata("unsedObjectWithExtensionFromObject.kt")
    public void testUnsedObjectWithExtensionFromObject() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/unsedObjectWithExtensionFromObject.kt");
    }

    @Test
    @TestMetadata("unusedAliasedTypeImport.kt")
    public void testUnusedAliasedTypeImport() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/unusedAliasedTypeImport.kt");
    }

    @Test
    @TestMetadata("unusedExtensionFunctionFromObject_implicitReceiver.kt")
    public void testUnusedExtensionFunctionFromObject_implicitReceiver() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/unusedExtensionFunctionFromObject_implicitReceiver.kt");
    }

    @Test
    @TestMetadata("unusedFunctionImportedFromObjectSuperClass.kt")
    public void testUnusedFunctionImportedFromObjectSuperClass() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/unusedFunctionImportedFromObjectSuperClass.kt");
    }

    @Test
    @TestMetadata("unusedFunctionImports.kt")
    public void testUnusedFunctionImports() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/unusedFunctionImports.kt");
    }

    @Test
    @TestMetadata("unusedGenericTypeQualifier.kt")
    public void testUnusedGenericTypeQualifier() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/unusedGenericTypeQualifier.kt");
    }

    @Test
    @TestMetadata("unusedImportFromObject.kt")
    public void testUnusedImportFromObject() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/unusedImportFromObject.kt");
    }

    @Test
    @TestMetadata("unusedInvokeOperatorImport.kt")
    public void testUnusedInvokeOperatorImport() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/unusedInvokeOperatorImport.kt");
    }

    @Test
    @TestMetadata("usedAliasedFunctionReference.kt")
    public void testUsedAliasedFunctionReference() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/usedAliasedFunctionReference.kt");
    }

    @Test
    @TestMetadata("usedAliasedTypeImport.kt")
    public void testUsedAliasedTypeImport() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/usedAliasedTypeImport.kt");
    }

    @Test
    @TestMetadata("usedExtensionFunctionFromObject_implicitReceiver.kt")
    public void testUsedExtensionFunctionFromObject_implicitReceiver() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/usedExtensionFunctionFromObject_implicitReceiver.kt");
    }

    @Test
    @TestMetadata("usedExtensionFunction_objectReceiver.kt")
    public void testUsedExtensionFunction_objectReceiver() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/usedExtensionFunction_objectReceiver.kt");
    }

    @Test
    @TestMetadata("usedFunctionImport.kt")
    public void testUsedFunctionImport() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/usedFunctionImport.kt");
    }

    @Test
    @TestMetadata("usedFunctionImportedFromObjectSuperClass.kt")
    public void testUsedFunctionImportedFromObjectSuperClass() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/usedFunctionImportedFromObjectSuperClass.kt");
    }

    @Test
    @TestMetadata("usedGenericTypeQualifier.kt")
    public void testUsedGenericTypeQualifier() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/usedGenericTypeQualifier.kt");
    }

    @Test
    @TestMetadata("usedImportFromObject.kt")
    public void testUsedImportFromObject() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/usedImportFromObject.kt");
    }

    @Test
    @TestMetadata("usedInvokeOperatorAliasedImport.kt")
    public void testUsedInvokeOperatorAliasedImport() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/usedInvokeOperatorAliasedImport.kt");
    }

    @Test
    @TestMetadata("usedInvokeOperatorExplicitImport.kt")
    public void testUsedInvokeOperatorExplicitImport() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/usedInvokeOperatorExplicitImport.kt");
    }

    @Test
    @TestMetadata("usedInvokeOperatorImport.kt")
    public void testUsedInvokeOperatorImport() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/usedInvokeOperatorImport.kt");
    }

    @Test
    @TestMetadata("usedTypeAsTypeParameter.kt")
    public void testUsedTypeAsTypeParameter() throws Exception {
        runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/usedTypeAsTypeParameter.kt");
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors")
    @TestDataPath("$PROJECT_ROOT")
    public class ReferencesWithErrors {
        @Test
        public void testAllFilesPresentInReferencesWithErrors() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors"), Pattern.compile("^([^.]+)\\.kt$"), null, true);
        }

        @Test
        @TestMetadata("unresolvedFunction.kt")
        public void testUnresolvedFunction() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/unresolvedFunction.kt");
        }

        @Test
        @TestMetadata("unresolvedFunctionStarImport.kt")
        public void testUnresolvedFunctionStarImport() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/unresolvedFunctionStarImport.kt");
        }

        @Test
        @TestMetadata("unresolvedProperty.kt")
        public void testUnresolvedProperty() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/unresolvedProperty.kt");
        }

        @Test
        @TestMetadata("unresolvedType.kt")
        public void testUnresolvedType() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/unresolvedType.kt");
        }

        @Test
        @TestMetadata("unresolvedTypeArgument.kt")
        public void testUnresolvedTypeArgument() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/unresolvedTypeArgument.kt");
        }

        @Test
        @TestMetadata("unresolvedTypeQualifier.kt")
        public void testUnresolvedTypeQualifier() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/unresolvedTypeQualifier.kt");
        }

        @Test
        @TestMetadata("unresolvedTypeQualifierConstructor.kt")
        public void testUnresolvedTypeQualifierConstructor() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/unresolvedTypeQualifierConstructor.kt");
        }

        @Test
        @TestMetadata("unresolvedViaImportAlias.kt")
        public void testUnresolvedViaImportAlias() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/unresolvedViaImportAlias.kt");
        }

        @Test
        @TestMetadata("unusedTypeHiddenByTypeParameter_invalidAsArgument.kt")
        public void testUnusedTypeHiddenByTypeParameter_invalidAsArgument() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/unusedTypeHiddenByTypeParameter_invalidAsArgument.kt");
        }

        @Test
        @TestMetadata("unusedUnresolvedImport.kt")
        public void testUnusedUnresolvedImport() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/unusedUnresolvedImport.kt");
        }

        @Test
        @TestMetadata("usedConstructor_invalidArguments.kt")
        public void testUsedConstructor_invalidArguments() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/usedConstructor_invalidArguments.kt");
        }

        @Test
        @TestMetadata("usedConstructor_missingCall.kt")
        public void testUsedConstructor_missingCall() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/usedConstructor_missingCall.kt");
        }

        @Test
        @TestMetadata("usedExtensionFunction_invalidArguments.kt")
        public void testUsedExtensionFunction_invalidArguments() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/usedExtensionFunction_invalidArguments.kt");
        }

        @Test
        @TestMetadata("usedExtensionProperty_invalidReceiver.kt")
        public void testUsedExtensionProperty_invalidReceiver() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/usedExtensionProperty_invalidReceiver.kt");
        }

        @Test
        @TestMetadata("usedInvokeOperator_invalidArguments.kt")
        public void testUsedInvokeOperator_invalidArguments() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/usedInvokeOperator_invalidArguments.kt");
        }

        @Test
        @TestMetadata("usedTypeAsTypeParameter_missingOuterType.kt")
        public void testUsedTypeAsTypeParameter_missingOuterType() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/usedTypeAsTypeParameter_missingOuterType.kt");
        }

        @Test
        @TestMetadata("usedTypeImport_missingGeneric.kt")
        public void testUsedTypeImport_missingGeneric() throws Exception {
            runTest("analysis/analysis-api/testData/components/importOptimizer/analyseImports/referencesWithErrors/usedTypeImport_missingGeneric.kt");
        }
    }
}
