package com.javiersc.kotlin.kopy.gradle.plugin

import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory

public open class KopyExtension
@Inject
constructor(
    private val providers: ProviderFactory,
    objects: ObjectFactory,
) {

    public companion object {
        public const val NAME: String = "kopy"
    }
}
