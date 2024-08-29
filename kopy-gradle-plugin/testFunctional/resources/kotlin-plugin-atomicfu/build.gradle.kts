plugins {
    application
    id("com.javiersc.kotlin.kopy")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.atomicfu")
}

application {
    mainClass = "com.javiersc.kotlin.kopy.functional.test.MainKt"
}

kopy {

}
