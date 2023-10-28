@file:Suppress("ComplexCondition")

import java.io.File
import java.nio.file.Paths

val keys =
    args.joinToString("\n").substringAfter("{").substringBefore("}").lines().mapNotNull { line ->
        val regex = """^([a-z_]+:).*""".toRegex()
        if (line.trim().matches(regex)) {
            val alteredLine = line.trim().substringBefore(":").removeSurrounding("\"")
            alteredLine
        } else {
            null
        }
    }
val values = args.joinToString("\n").substringAfter("|||").substringBefore("|||").split(",,")

val argsMap = keys.zip(values) { key, value -> key to value }.toMap()

for ((key, value) in argsMap) {
    println("$key: $value")
}

val String.placeholder: String
    get() = """"{{ $this }}""""

println("ARGS: \n${argsMap.toList().joinToString("\n"){ (key, value) -> "ARG: $key: $value"}}")

val currentDir: File = Paths.get("").toAbsolutePath().toFile()

println("FILE: ${currentDir.path}")

currentDir.walkTopDown().forEach { file ->
    if (
        file.isFile &&
            file.name != "empty.file" &&
            file.name != "initial-setup.main.kts" &&
            file.name != "initial-setup.yaml" &&
            file.name != "gradlew" &&
            file.name != "gradlew.bat" &&
            file.path.contains("${File.separator}.git${File.separator}").not() &&
            file.path.contains("${File.separator}.gradle${File.separator}").not() &&
            file.path.contains("${File.separator}build${File.separator}").not() &&
            file.path
                .contains("${File.separator}gradle${File.separator}wrapper${File.separator}")
                .not() &&
            file.path.contains("${File.separator}.idea${File.separator}").not()
    ) {
        println("CHECKING FILE: $file...")
        val newContent =
            file.readLines().joinToString("\n") { line ->
                val filteredArgs =
                    argsMap
                        .filter { (key, _) -> line.contains(key.placeholder) }
                        .mapKeys { (key, _) -> key.placeholder }

                when {
                    filteredArgs.isNotEmpty() -> {
                        line
                            .replace(filteredArgs)
                            .replace("#TODO: ", "")
                            .replace("# TODO: ", "")
                            .replace("//TODO: ", "")
                            .replace("// TODO: ", "")
                    }
                    listOf(
                            """#TODO: Uncomment"{{""",
                            """# TODO: Uncomment"{{""",
                            """//TODO: Uncomment"{{""",
                            """// TODO: Uncomment"{{""",
                        )
                        .any { line.contains(it) } -> {
                        println("UNCOMMENT FOUND: $line")
                        line
                            .replace("""#TODO: Uncomment"{{ """, "")
                            .replace("""# TODO: Uncomment"{{ """, "")
                            .replace("""//TODO: Uncomment"{{ """, "")
                            .replace("""// TODO: Uncomment"{{ """, "")
                            .replace(""" }}"""", "")
                    }
                    else -> line
                }
            }
        file.writeText(newContent + "\n")
    }
}

File(".github/workflows/initial-setup.yaml").delete()

File(".github/workflows/build.yaml").delete()

File(".github/workflows/publish.yaml").delete()

File("initial-setup.main.kts").delete()

File("README.md").writeText("# ${argsMap["name"]}\n")

fun String.replace(replacements: Map<String, String>): String =
    replace(*replacements.toList().toTypedArray())

fun String.replace(vararg replacements: Pair<String, String>): String {
    var result = this
    for ((l, r) in replacements) {
        result = result.replace(l, r)
    }
    return result
}
