package com.javiersc.kotlin.kopy.gradle.plugin

import com.javiersc.kotlin.kopy.args.KopyCopyFunctions
import com.javiersc.kotlin.kopy.args.KopyTransformFunctions
import com.javiersc.kotlin.kopy.args.KopyVisibility
import javax.inject.Inject
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

public open class KopyExtension
@Inject
constructor(private val providers: ProviderFactory, objects: ObjectFactory, layout: ProjectLayout) {

    public val debug: Property<Boolean> = objects.property<Boolean>().convention(false)

    public val copyFunctions: ListProperty<KopyCopyFunctions> =
        objects.listProperty<KopyCopyFunctions>().convention(KopyCopyFunctions.entries)

    public val reportPath: DirectoryProperty =
        objects.directoryProperty().convention(layout.buildDirectory.dir("reports/kopy/"))

    public val transformFunctions: ListProperty<KopyTransformFunctions> =
        objects.listProperty<KopyTransformFunctions>().convention(KopyTransformFunctions.entries)

    public val visibility: Property<KopyVisibility> =
        objects.property(KopyVisibility::class.java).convention(KopyVisibility.Auto)

    public companion object {
        public const val NAME: String = "kopy"
    }
}
