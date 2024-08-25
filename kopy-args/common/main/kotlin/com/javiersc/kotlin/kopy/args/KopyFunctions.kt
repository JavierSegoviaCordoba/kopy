package com.javiersc.kotlin.kopy.args

import java.io.Serializable

public enum class KopyFunctions(public val value: String) : Serializable {
    All(value = "all"),
    Copy(value = "copy"),
    Invoke(value = "invoke"),
    ;

    public companion object {

        public const val NAME: String = "KopyFunctions"
        public const val DESCRIPTION: String = "Kopy functions to be generated"

        public fun from(value: String): KopyFunctions =
            when (value) {
                All.name -> All
                All.value -> All
                Copy.name -> Copy
                Copy.value -> Copy
                Invoke.name -> Invoke
                Invoke.value -> Invoke
                else -> All
            }
    }
}
