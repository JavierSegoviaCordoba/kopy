package com.javiersc.kotlin.kopy.compiler.fir

import com.javiersc.kotlin.kopy.compiler.KopyConfig
import com.javiersc.kotlin.kopy.compiler.fir.checker.FirKopyCheckerExtension
import com.javiersc.kotlin.kopy.compiler.fir.generation.FirKopyAssignExpressionAltererExtension
import com.javiersc.kotlin.kopy.compiler.fir.generation.FirKopyDeclarationGenerationExtension
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

internal class FirKopyExtension(private val kopyConfig: KopyConfig) : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        registerCheckers()
        registerGenerators()
    }

    private fun ExtensionRegistrarContext.registerCheckers() {
        +{ session: FirSession -> FirKopyCheckerExtension(session, kopyConfig) }
    }

    private fun ExtensionRegistrarContext.registerGenerators() {
        +{ session: FirSession -> FirKopyAssignExpressionAltererExtension(session, kopyConfig) }
        +{ session: FirSession -> FirKopyDeclarationGenerationExtension(session, kopyConfig) }
    }
}

internal object Key : GeneratedDeclarationKey()
