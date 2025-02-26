package com.javiersc.kotlin.kopy.args

import java.io.Serializable

public enum class KopyTransformFunctions(public val value: String) : Serializable {
    Set(value = "set"),
    Update(value = "update"),
    UpdateEach(value = "updateEach");

    public companion object {

        public const val NAME: String = "KopyTransformFunctions"
        public const val VALUE_DESCRIPTION: String = "<set|update|updateEach>"
        public const val DESCRIPTION: String = "Kopy transform functions to be generated"

        public fun from(value: String): List<KopyTransformFunctions> {
            val values: List<String> = value.split(",")
            return from(values)
        }

        public fun from(values: List<String>): List<KopyTransformFunctions> {
            if (values.isEmpty()) return entries
            return values.map { value ->
                when (value.lowercase()) {
                    Set.value -> Set
                    Update.value -> Update
                    UpdateEach.value.lowercase() -> UpdateEach
                    else -> error("Unknown 'KopyTransformFunction'")
                }
            }
        }

        public fun asString(values: List<KopyTransformFunctions>): String =
            values.joinToString(",") { it.value }
    }
}
