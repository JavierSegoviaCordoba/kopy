plugins {
    alias(libs.plugins.javiersc.hubdle)
}

hubdle {
    config {
        analysis()
        binaryCompatibilityValidator()
        coverage()
        documentation {
            api()
            changelog()
            readme {
                badges()
            }
            site()
        }
        nexus()
    }
}
