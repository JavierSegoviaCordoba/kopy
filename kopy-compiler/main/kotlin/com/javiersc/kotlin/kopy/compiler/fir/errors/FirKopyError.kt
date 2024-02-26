package com.javiersc.kotlin.kopy.compiler.fir.errors

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.Renderer
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory

internal object FirKopyError : BaseDiagnosticRendererFactory() {

    init {
        RootDiagnosticRendererFactory.registerFactory(FirKopyError)
    }

    val NON_DATA_CLASS_KOPY_ANNOTATED: KtDiagnosticFactory1<String> by error1<PsiElement, String>()

    val INVALID_CALL_CHAIN: KtDiagnosticFactory1<String> by error1<PsiElement, String>()

    val MISSING_DATA_CLASS: KtDiagnosticFactory1<String> by error1<PsiElement, String>()

    val MISSING_KOPY_ANNOTATION: KtDiagnosticFactory1<String> by error1<PsiElement, String>()

    override val MAP: KtDiagnosticFactoryToRendererMap = rendererMap { map ->
        map.put(
            factory = NON_DATA_CLASS_KOPY_ANNOTATED,
            message = "The class `{0}` must be a data class",
            rendererA = Renderer { t: String -> t },
        )
        map.put(
            factory = INVALID_CALL_CHAIN,
            message = "Call chain broken at `{0}`",
            rendererA = Renderer { t: String -> t },
        )
        map.put(
            factory = MISSING_DATA_CLASS,
            message = "The property `{0}` does not belong to a data class",
            rendererA = Renderer { t: String -> t },
        )
        map.put(
            factory = MISSING_KOPY_ANNOTATION,
            message = "The property `{0}` does not belong to a data class annotated with `@Kopy`",
            rendererA = Renderer { t: String -> t },
        )
    }

    private fun rendererMap(
        block: (KtDiagnosticFactoryToRendererMap) -> Unit
    ): KtDiagnosticFactoryToRendererMap = KtDiagnosticFactoryToRendererMap("FirKopy").also(block)
}
