/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.analysis.api.GenerateAnalysisApiTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("analysis/low-level-api-fir/testData/dependentCopy")
@TestDataPath("$PROJECT_ROOT")
public class ScriptDependentCopyContextTestGenerated extends AbstractScriptDependentCopyContextTest {
    @Test
    public void testAllFilesPresentInDependentCopy() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/low-level-api-fir/testData/dependentCopy"), Pattern.compile("^(.+)\\.(kts)$"), null, true);
    }

    @Test
    @TestMetadata("classBodyScript.kts")
    public void testClassBodyScript() throws Exception {
        runTest("analysis/low-level-api-fir/testData/dependentCopy/classBodyScript.kts");
    }

    @Test
    @TestMetadata("classScript.kts")
    public void testClassScript() throws Exception {
        runTest("analysis/low-level-api-fir/testData/dependentCopy/classScript.kts");
    }

    @Test
    @TestMetadata("moreStatementsInCopy.kts")
    public void testMoreStatementsInCopy() throws Exception {
        runTest("analysis/low-level-api-fir/testData/dependentCopy/moreStatementsInCopy.kts");
    }

    @Test
    @TestMetadata("script.kts")
    public void testScript() throws Exception {
        runTest("analysis/low-level-api-fir/testData/dependentCopy/script.kts");
    }

    @Test
    @TestMetadata("scriptFunction.kts")
    public void testScriptFunction() throws Exception {
        runTest("analysis/low-level-api-fir/testData/dependentCopy/scriptFunction.kts");
    }

    @Test
    @TestMetadata("scriptInsideLastStatement.kts")
    public void testScriptInsideLastStatement() throws Exception {
        runTest("analysis/low-level-api-fir/testData/dependentCopy/scriptInsideLastStatement.kts");
    }

    @Test
    @TestMetadata("scriptLastStatement.kts")
    public void testScriptLastStatement() throws Exception {
        runTest("analysis/low-level-api-fir/testData/dependentCopy/scriptLastStatement.kts");
    }

    @Test
    @TestMetadata("scriptLastStatementCall.kts")
    public void testScriptLastStatementCall() throws Exception {
        runTest("analysis/low-level-api-fir/testData/dependentCopy/scriptLastStatementCall.kts");
    }

    @Test
    @TestMetadata("scriptStatement.kts")
    public void testScriptStatement() throws Exception {
        runTest("analysis/low-level-api-fir/testData/dependentCopy/scriptStatement.kts");
    }

    @Test
    @TestMetadata("scriptStatementCall.kts")
    public void testScriptStatementCall() throws Exception {
        runTest("analysis/low-level-api-fir/testData/dependentCopy/scriptStatementCall.kts");
    }
}
