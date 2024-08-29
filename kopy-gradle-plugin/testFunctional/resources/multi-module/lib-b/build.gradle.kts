plugins {
    application
    id("com.javiersc.kotlin.kopy")
    id("org.jetbrains.kotlin.jvm")
}

application {
    mainClass = "com.javiersc.kotlin.kopy.functional.lib.b.MainKt"
}

kopy {

}

dependencies {
    implementation(project(":lib-a"))
}
