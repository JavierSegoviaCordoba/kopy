plugins {
    id("com.javiersc.kotlin.kopy")
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    linuxArm64()
    linuxX64()
}

kopy {

}
