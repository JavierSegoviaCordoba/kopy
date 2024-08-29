package com.javiersc.kotlin.kopy.args

public enum class KopyVisibility(
    public val value: String,
    public val restrictive: Int,
) : KopyArgument {
    Auto(value = "auto", restrictive = 0),
    Public(value = "public", restrictive = 1),
    Internal(value = "internal", restrictive = 2),
    Protected(value = "protected", restrictive = 3),
    Private(value = "private", restrictive = 4),
    ;

    public companion object {

        public const val NAME: String = "KopyVisibility"
        public const val DESCRIPTION: String = "Visibility of the generated copy function"

        public fun from(value: String): KopyVisibility =
            when (value) {
                Auto.name -> Auto
                Auto.value -> Auto
                Public.name -> Public
                Public.value -> Public
                Internal.name -> Internal
                Internal.value -> Internal
                Protected.name -> Protected
                Protected.value -> Protected
                Private.name -> Private
                Private.value -> Private
                else -> Auto
            }
    }
}
