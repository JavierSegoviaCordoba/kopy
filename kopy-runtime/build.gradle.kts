import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

hubdle {
    config {
        analysis()
        coverage()
        documentation { //
            api()
        }
        explicitApi()
        publishing()
    }
    kotlin {
        jvm {
            features {
                contextReceivers()
            }
        }
    }
}
