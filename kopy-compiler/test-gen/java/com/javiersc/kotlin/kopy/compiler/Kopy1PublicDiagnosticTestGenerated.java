

package com.javiersc.kotlin.kopy.compiler;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link com.javiersc.kotlin.kopy.compiler.GenerateKotlinCompilerTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("test-data/diagnostics-kopy-visibility/1_public")
@TestDataPath("$PROJECT_ROOT")
public class Kopy1PublicDiagnosticTestGenerated extends AbstractKopy1PublicDiagnosticTest {
  @Test
  public void testAllFilesPresentIn1_public() {
    KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("test-data/diagnostics-kopy-visibility/1_public"), Pattern.compile("^(.+)\\.kt$"), null, true);
  }

  @Test
  @TestMetadata("internal.kt")
  public void testInternal() {
    runTest("test-data/diagnostics-kopy-visibility/1_public/internal.kt");
  }

  @Test
  @TestMetadata("private.kt")
  public void testPrivate() {
    runTest("test-data/diagnostics-kopy-visibility/1_public/private.kt");
  }

  @Test
  @TestMetadata("protected.kt")
  public void testProtected() {
    runTest("test-data/diagnostics-kopy-visibility/1_public/protected.kt");
  }

  @Test
  @TestMetadata("public.kt")
  public void testPublic() {
    runTest("test-data/diagnostics-kopy-visibility/1_public/public.kt");
  }
}