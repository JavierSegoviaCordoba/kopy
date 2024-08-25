package com.javiersc.kotlin.kopy.gradle.plugin

import com.javiersc.kotlin.kopy.args.KopyFunctions
import com.javiersc.kotlin.kopy.args.KopyVisibility
import javax.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory

public open class KopyExtension
@Inject
constructor(
    private val providers: ProviderFactory,
    objects: ObjectFactory,
) {

    public val functions: Property<KopyFunctions> =
        objects.property(KopyFunctions::class.java).convention(KopyFunctions.All)

    public val visibility: Property<KopyVisibility> =
        objects.property(KopyVisibility::class.java).convention(KopyVisibility.Auto)

    public companion object {
        public const val NAME: String = "kopy"
    }
}
