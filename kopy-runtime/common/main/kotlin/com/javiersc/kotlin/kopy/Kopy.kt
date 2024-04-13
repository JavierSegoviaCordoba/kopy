package com.javiersc.kotlin.kopy

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
public annotation class Kopy {

    public companion object
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class KopyFunctionInvoke {

    public companion object
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class KopyFunctionCopy {

    public companion object
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class KopyFunctionSet {

    public companion object
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class KopyFunctionUpdate {

    public companion object
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class KopyFunctionUpdateEach {

    public companion object
}
