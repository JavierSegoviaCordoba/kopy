// RUN_PIPELINE_TILL: FRONTEND
// OPT_IN: com.javiersc.kotlin.kopy.KopyOptIn

package com.javiersc.kotlin.kopy.playground

import com.javiersc.kotlin.kopy.Kopy

<!NON_DATA_CLASS_KOPY_ANNOTATED!>@Kopy<!> class Foo(val value: String)

/* GENERATED_FIR_TAGS: classDeclaration, primaryConstructor, propertyDeclaration */
