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
            features {
                jvmVersion(JavaVersion.VERSION_1_8)
                kotest.enabled(false)
            }
            common()
            androidNative {
                androidNativeArm32()
                androidNativeArm64()
                androidNativeX64()
                androidNativeX86()
            }
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
            mingw { //
                mingwX64()
            }
            native()
            wasm {
                js {
                    browser()
                    d8()
                    nodejs()
                }
                wasi { //
                    nodejs()
                }
            }
        }
    }
}
