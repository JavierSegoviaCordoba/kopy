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
            common {
                main {
                    dependencies { //
                        api(hubdle.jetbrains.kotlinx.atomicfu)
                    }
                }
            }
            jvm()
        }
    }
}
