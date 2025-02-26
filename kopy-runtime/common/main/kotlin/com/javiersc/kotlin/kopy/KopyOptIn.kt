package com.javiersc.kotlin.kopy

@RequiresOptIn(
    message =
        """The Kopy plugin must be applied to use this API:
             • https://github.com/JavierSegoviaCordoba/kopy
        """
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
public annotation class KopyOptIn
