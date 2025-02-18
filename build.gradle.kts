import com.javiersc.kotlin.stdlib.AnsiColor.Foreground
import com.javiersc.kotlin.stdlib.ansiColor

plugins { //
    alias(libs.plugins.javiersc.hubdle)
}

hubdle {
    config {
        analysis()
        // binaryCompatibilityValidator()
        coverage()
        documentation {
            api()
            changelog()
            readme { //
                badges()
            }
            site()
        }
        nexus()
    }
}


repeat(3) {
    val command = "'./gradlew assemble publishToMavenLocalTest'"
    logger.warn("Remember to run $command before running the tests!!!".ansiColor(Foreground.Yellow))
}
