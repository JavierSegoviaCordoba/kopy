hubdle {
    config {
        analysis()
        coverage()
        documentation { //
            api()
        }
        explicitApi()
        publishing {
            maven {
                repositories { //
                    mavenLocalTest()
                }
            }
        }
    }
    kotlin {
        jvm {
            features { //
                jvmVersion(JavaVersion.VERSION_1_8)
                kotest.enabled(true)
            }
        }
    }
}
