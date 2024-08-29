hubdle {
    config {
        analysis()
        coverage()
        documentation { //
            api()
        }
        explicitApi()
        format.isEnabled = false
        publishing {
            maven {
                repositories { //
                    mavenLocalTest()
                }
            }
        }
    }
    kotlin {
        multiplatform {
            common()
            androidNative()
            apple {
                ios {
                    iosArm64()
                    iosSimulatorArm64()
                    iosX64()
                }
                macos {
                    macosArm64()
                    macosX64()
                }
                tvos {
                    tvosArm64()
                    tvosSimulatorArm64()
                    tvosX64()
                }
            }
            jvm()
            js {
                browser()
                nodejs()
            }
            linux {
                linuxArm64()
                linuxX64()
            }
            mingw {
                mingwX64()
            }
            native()
            wasmJs()
        }
    }
}
