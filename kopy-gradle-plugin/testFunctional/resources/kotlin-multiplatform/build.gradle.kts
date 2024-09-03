plugins {
    id("com.javiersc.kotlin.kopy")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.atomicfu")
}

kotlin {
    jvm()
    linuxArm64()
    macosArm64()
    mingwX64()
}

kopy {

}
