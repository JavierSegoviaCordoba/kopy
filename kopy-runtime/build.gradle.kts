hubdle {
    config {
        analysis()
        coverage()
        documentation { //
            api()
        }
        explicitApi()
        format.isEnabled = false
        publishing()
    }
    kotlin {
        jvm {
            features { //
                contextReceivers()
            }

            main {
                dependencies { //
                    implementation(hubdle.jetbrains.kotlinx.atomicfu)
                }
            }
        }
    }
}
