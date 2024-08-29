import com.javiersc.kotlin.kopy.args.KopyVisibility

plugins {
    application
    id("com.javiersc.kotlin.kopy")
    id("org.jetbrains.kotlin.jvm")
}

application {
    mainClass = "com.javiersc.kotlin.kopy.functional.test.MainKt"
}

kopy {
    visibility.set(KopyVisibility.Private)
}
