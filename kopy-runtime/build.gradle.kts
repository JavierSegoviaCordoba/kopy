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
        multiplatform { //
            jvm()
        }
    }
}
