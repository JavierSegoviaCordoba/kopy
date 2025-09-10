package com.javiersc.kotlin.kopy.compiler.fir.checker

import com.intellij.psi.PsiElement
import com.javiersc.kotlin.kopy.compiler.fir.checker.FirKopyError.ARGUMENT_TYPE_MISMATCH
import com.javiersc.kotlin.kopy.compiler.fir.checker.FirKopyError.INVALID_CALL_CHAIN
import com.javiersc.kotlin.kopy.compiler.fir.checker.FirKopyError.MISSING_DATA_CLASS
import com.javiersc.kotlin.kopy.compiler.fir.checker.FirKopyError.MISSING_KOPY_ANNOTATION
import com.javiersc.kotlin.kopy.compiler.fir.checker.FirKopyError.NON_DATA_CLASS_KOPY_ANNOTATED
import com.javiersc.kotlin.kopy.compiler.fir.checker.FirKopyError.NO_COPY_SCOPE
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory2
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.error2
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.Renderer

internal object FirKopyError : KtDiagnosticsContainer() {

    val NON_DATA_CLASS_KOPY_ANNOTATED: KtDiagnosticFactory1<String> by error1<PsiElement, String>()

    val INVALID_CALL_CHAIN: KtDiagnosticFactory1<String> by error1<PsiElement, String>()

    val MISSING_DATA_CLASS: KtDiagnosticFactory1<String> by error1<PsiElement, String>()

    val MISSING_KOPY_ANNOTATION: KtDiagnosticFactory1<String> by error1<PsiElement, String>()

    val NO_COPY_SCOPE: KtDiagnosticFactory1<String> by error1<PsiElement, String>()

    val ARGUMENT_TYPE_MISMATCH: KtDiagnosticFactory2<String, String> by
        error2<PsiElement, String, String>()

    override fun getRendererFactory(): BaseDiagnosticRendererFactory = Renderers

    private object Renderers : BaseDiagnosticRendererFactory() {

        override val MAP: KtDiagnosticFactoryToRendererMap by rendererMap
    }
}

private val rendererMap: Lazy<KtDiagnosticFactoryToRendererMap> =
    KtDiagnosticFactoryToRendererMap("Kopy errors") { map ->
        map.put(
            factory = NON_DATA_CLASS_KOPY_ANNOTATED,
            message = "The class ''{0}'' must be a data class",
            rendererA = Renderer { t: String -> t },
        )
        map.put(
            factory = INVALID_CALL_CHAIN,
            message = "Call chain broken at ''{0}''",
            rendererA = Renderer { t: String -> t },
        )
        map.put(
            factory = MISSING_DATA_CLASS,
            message = "The property ''{0}'' does not belong to a data class",
            rendererA = Renderer { t: String -> t },
        )
        map.put(
            factory = MISSING_KOPY_ANNOTATION,
            message =
                "The property ''{0}'' does not belong to a data class annotated with ''@Kopy''",
            rendererA = Renderer { t: String -> t },
        )
        map.put(
            factory = NO_COPY_SCOPE,
            message =
                "Invalid scope ''{0}'', ''set/update'' are only allowed inside a ''copy'' scope",
            rendererA = Renderer { t: String -> t },
        )
        map.put(
            factory = ARGUMENT_TYPE_MISMATCH,
            message = "Argument type mismatch: actual type is ''{1}'', but ''{0}'' was expected.",
            rendererA = Renderer { t: String -> t },
            rendererB = Renderer { t: String -> t },
        )
    }
