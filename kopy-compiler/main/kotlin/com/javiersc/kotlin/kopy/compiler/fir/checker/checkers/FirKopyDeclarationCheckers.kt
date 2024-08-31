package com.javiersc.kotlin.kopy.compiler.fir.checker.checkers

import com.javiersc.kotlin.compiler.extensions.fir.name
import com.javiersc.kotlin.kopy.compiler.fir.checker.FirKopyError
import com.javiersc.kotlin.kopy.compiler.kopyClassId
import com.javiersc.kotlin.kopy.compiler.kopyFqName
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassLikeChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.isData
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.renderer.render

internal object FirKopyDeclarationCheckers : DeclarationCheckers() {
    override val classLikeCheckers: Set<FirClassLikeChecker> =
        setOf(
            DataClassKopyAnnotationChecker,
        )
}

private object DataClassKopyAnnotationChecker :
    FirDeclarationChecker<FirClassLikeDeclaration>(MppCheckerKind.Common) {

    override fun check(
        declaration: FirClassLikeDeclaration,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        val isKopy: Boolean = declaration.hasAnnotation(kopyClassId, context.session)
        val isDataClass: Boolean = declaration.symbol.isData
        if (isKopy && !isDataClass) {
            val kopyAnnotation: FirAnnotation =
                declaration.annotations.first { it.fqName(context.session) == kopyFqName }
            reporter.reportOn(
                source = kopyAnnotation.source,
                factory = FirKopyError.NON_DATA_CLASS_KOPY_ANNOTATED,
                a = declaration.name.render(),
                context = context,
                positioningStrategy = SourceElementPositioningStrategies.DEFAULT,
            )
        }
    }
}
