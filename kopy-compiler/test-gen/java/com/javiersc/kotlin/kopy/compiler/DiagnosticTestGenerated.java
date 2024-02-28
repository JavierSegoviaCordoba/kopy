

package com.javiersc.kotlin.kopy.compiler;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link com.javiersc.kotlin.kopy.compiler.GenerateKotlinCompilerTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("test-data/diagnostics")
@TestDataPath("$PROJECT_ROOT")
public class DiagnosticTestGenerated extends AbstractDiagnosticTest {
    @Test
    public void testAllFilesPresentInDiagnostics() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("test-data/diagnostics"), Pattern.compile("^(.+)\\.kt$"), null, true);
    }

    @Nested
    @TestMetadata("test-data/diagnostics/edge")
    @TestDataPath("$PROJECT_ROOT")
    public class Edge {
        @Test
        public void testAllFilesPresentInEdge() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("test-data/diagnostics/edge"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @Test
        @TestMetadata("no-nest-copy-set.kt")
        public void testNo_nest_copy_set() throws Exception {
            runTest("test-data/diagnostics/edge/no-nest-copy-set.kt");
        }
    }

    @Nested
    @TestMetadata("test-data/diagnostics/invalid-call-chain")
    @TestDataPath("$PROJECT_ROOT")
    public class Invalid_call_chain {
        @Test
        public void testAllFilesPresentInInvalid_call_chain() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("test-data/diagnostics/invalid-call-chain"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @Test
        @TestMetadata("complex-1.kt")
        public void testComplex_1() throws Exception {
            runTest("test-data/diagnostics/invalid-call-chain/complex-1.kt");
        }

        @Test
        @TestMetadata("simple-1.kt")
        public void testSimple_1() throws Exception {
            runTest("test-data/diagnostics/invalid-call-chain/simple-1.kt");
        }
    }

    @Nested
    @TestMetadata("test-data/diagnostics/missing-data-class")
    @TestDataPath("$PROJECT_ROOT")
    public class Missing_data_class {
        @Test
        public void testAllFilesPresentInMissing_data_class() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("test-data/diagnostics/missing-data-class"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @Test
        @TestMetadata("complex-1.kt")
        public void testComplex_1() throws Exception {
            runTest("test-data/diagnostics/missing-data-class/complex-1.kt");
        }

        @Test
        @TestMetadata("simple-1.kt")
        public void testSimple_1() throws Exception {
            runTest("test-data/diagnostics/missing-data-class/simple-1.kt");
        }

        @Test
        @TestMetadata("simple-2.kt")
        public void testSimple_2() throws Exception {
            runTest("test-data/diagnostics/missing-data-class/simple-2.kt");
        }

        @Test
        @TestMetadata("simple-3.kt")
        public void testSimple_3() throws Exception {
            runTest("test-data/diagnostics/missing-data-class/simple-3.kt");
        }

        @Test
        @TestMetadata("simple-4.kt")
        public void testSimple_4() throws Exception {
            runTest("test-data/diagnostics/missing-data-class/simple-4.kt");
        }

        @Test
        @TestMetadata("simple-5.kt")
        public void testSimple_5() throws Exception {
            runTest("test-data/diagnostics/missing-data-class/simple-5.kt");
        }

        @Test
        @TestMetadata("simple-6.kt")
        public void testSimple_6() throws Exception {
            runTest("test-data/diagnostics/missing-data-class/simple-6.kt");
        }

        @Test
        @TestMetadata("simple-7.kt")
        public void testSimple_7() throws Exception {
            runTest("test-data/diagnostics/missing-data-class/simple-7.kt");
        }
    }

    @Nested
    @TestMetadata("test-data/diagnostics/non-data-class")
    @TestDataPath("$PROJECT_ROOT")
    public class Non_data_class {
        @Test
        public void testAllFilesPresentInNon_data_class() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("test-data/diagnostics/non-data-class"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @Test
        @TestMetadata("simple-1.kt")
        public void testSimple_1() throws Exception {
            runTest("test-data/diagnostics/non-data-class/simple-1.kt");
        }
    }

    @Nested
    @TestMetadata("test-data/diagnostics/valid")
    @TestDataPath("$PROJECT_ROOT")
    public class Valid {
        @Test
        public void testAllFilesPresentInValid() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("test-data/diagnostics/valid"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @Test
        @TestMetadata("complex-1.kt")
        public void testComplex_1() throws Exception {
            runTest("test-data/diagnostics/valid/complex-1.kt");
        }

        @Test
        @TestMetadata("simple-1.kt")
        public void testSimple_1() throws Exception {
            runTest("test-data/diagnostics/valid/simple-1.kt");
        }
    }
}
