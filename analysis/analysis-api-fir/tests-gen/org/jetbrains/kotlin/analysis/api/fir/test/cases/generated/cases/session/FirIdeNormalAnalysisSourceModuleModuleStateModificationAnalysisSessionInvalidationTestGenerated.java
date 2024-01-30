/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.test.cases.generated.cases.session;

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
import org.jetbrains.kotlin.analysis.api.impl.base.test.cases.session.AbstractModuleStateModificationAnalysisSessionInvalidationTest;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.analysis.api.GenerateAnalysisApiTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("analysis/analysis-api/testData/sessions/sessionInvalidation")
@TestDataPath("$PROJECT_ROOT")
public class FirIdeNormalAnalysisSourceModuleModuleStateModificationAnalysisSessionInvalidationTestGenerated extends AbstractModuleStateModificationAnalysisSessionInvalidationTest {
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
    public void testAllFilesPresentInSessionInvalidation() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/sessions/sessionInvalidation"), Pattern.compile("^(.+)\\.kt$"), null, true);
    }

    @Test
    @TestMetadata("binaryTreeAdditionalEdge.kt")
    public void testBinaryTreeAdditionalEdge() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/binaryTreeAdditionalEdge.kt");
    }

    @Test
    @TestMetadata("binaryTreeInvalidateA.kt")
    public void testBinaryTreeInvalidateA() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/binaryTreeInvalidateA.kt");
    }

    @Test
    @TestMetadata("binaryTreeInvalidateCD.kt")
    public void testBinaryTreeInvalidateCD() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/binaryTreeInvalidateCD.kt");
    }

    @Test
    @TestMetadata("binaryTreeInvalidateF.kt")
    public void testBinaryTreeInvalidateF() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/binaryTreeInvalidateF.kt");
    }

    @Test
    @TestMetadata("binaryTreeInvalidateL1.kt")
    public void testBinaryTreeInvalidateL1() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/binaryTreeInvalidateL1.kt");
    }

    @Test
    @TestMetadata("binaryTreeInvalidateL2.kt")
    public void testBinaryTreeInvalidateL2() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/binaryTreeInvalidateL2.kt");
    }

    @Test
    @TestMetadata("binaryTreeInvalidateLibrarySourceL1.kt")
    public void testBinaryTreeInvalidateLibrarySourceL1() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/binaryTreeInvalidateLibrarySourceL1.kt");
    }

    @Test
    @TestMetadata("binaryTreeInvalidateLibrarySourceL2.kt")
    public void testBinaryTreeInvalidateLibrarySourceL2() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/binaryTreeInvalidateLibrarySourceL2.kt");
    }

    @Test
    @TestMetadata("binaryTreeInvalidateNone.kt")
    public void testBinaryTreeInvalidateNone() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/binaryTreeInvalidateNone.kt");
    }

    @Test
    @TestMetadata("linearInvalidateC.kt")
    public void testLinearInvalidateC() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/linearInvalidateC.kt");
    }

    @Test
    @TestMetadata("linearInvalidateL1.kt")
    public void testLinearInvalidateL1() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/linearInvalidateL1.kt");
    }

    @Test
    @TestMetadata("linearInvalidateLibrarySourceL1.kt")
    public void testLinearInvalidateLibrarySourceL1() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/linearInvalidateLibrarySourceL1.kt");
    }

    @Test
    @TestMetadata("rhombusInvalidateBC.kt")
    public void testRhombusInvalidateBC() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/rhombusInvalidateBC.kt");
    }

    @Test
    @TestMetadata("rhombusInvalidateCD.kt")
    public void testRhombusInvalidateCD() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/rhombusInvalidateCD.kt");
    }

    @Test
    @TestMetadata("rhombusInvalidateD.kt")
    public void testRhombusInvalidateD() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/rhombusInvalidateD.kt");
    }

    @Test
    @TestMetadata("rhombusInvalidateL1.kt")
    public void testRhombusInvalidateL1() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/rhombusInvalidateL1.kt");
    }

    @Test
    @TestMetadata("rhombusInvalidateL2.kt")
    public void testRhombusInvalidateL2() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/rhombusInvalidateL2.kt");
    }

    @Test
    @TestMetadata("rhombusInvalidateLibrarySourceL1.kt")
    public void testRhombusInvalidateLibrarySourceL1() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/rhombusInvalidateLibrarySourceL1.kt");
    }

    @Test
    @TestMetadata("rhombusInvalidateLibrarySourceL2.kt")
    public void testRhombusInvalidateLibrarySourceL2() throws Exception {
        runTest("analysis/analysis-api/testData/sessions/sessionInvalidation/rhombusInvalidateLibrarySourceL2.kt");
    }
}
