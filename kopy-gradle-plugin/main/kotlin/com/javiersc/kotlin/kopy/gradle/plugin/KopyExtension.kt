package com.javiersc.kotlin.kopy.gradle.plugin

import com.javiersc.kotlin.kopy.args.KopyFunctions
import com.javiersc.kotlin.kopy.args.KopyVisibility
import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.property

public open class KopyExtension
@Inject
constructor(private val providers: ProviderFactory, objects: ObjectFactory, layout: ProjectLayout) {

    public val debug: Property<Boolean> = objects.property<Boolean>().convention(false)

    public val functions: Property<KopyFunctions> =
        objects.property(KopyFunctions::class.java).convention(KopyFunctions.All)

    public val reportPath: DirectoryProperty =
        objects.directoryProperty().convention(layout.buildDirectory.dir("reports/kopy/"))

    public val visibility: Property<KopyVisibility> =
        objects.property(KopyVisibility::class.java).convention(KopyVisibility.Auto)

    public companion object {
        public const val NAME: String = "kopy"
    }
}
