package com.javiersc.kotlin.kopy.args

import java.io.Serializable

public enum class KopyCopyFunctions(public val value: String) : Serializable {
    Copy(value = "copy"),
    Invoke(value = "invoke");

    public companion object {

        public const val NAME: String = "KopyCopyFunctions"
        public const val VALUE_DESCRIPTION: String = "<copy|invoke>"
        public const val DESCRIPTION: String = "Kopy functions to be generated"

        public fun asString(copyFunctions: List<KopyCopyFunctions>): String =
            copyFunctions.joinToString(",") { it.value }

        public fun from(value: String): List<KopyCopyFunctions> {
            val values: List<String> = value.split(",")
            return from(values)
        }

        public fun from(values: List<String>): List<KopyCopyFunctions> {
            if (values.isEmpty()) return KopyCopyFunctions.entries
            return values.map { value ->
                when (value.lowercase()) {
                    Copy.value -> Copy
                    Invoke.value -> Invoke
                    else -> error("Unknown 'KopyCopyFunctions'")
                }
            }
        }
    }
}
