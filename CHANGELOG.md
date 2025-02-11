# Changelog

## [Unreleased]

### Added

### Changed

### Deprecated

### Fixed

### Removed

### Updated

- `com.javiersc.hubdle:hubdle-version-catalog -> 0.4.3`

## [0.14.0+2.1.10] - 2025-01-28

### Updated

- `com.javiersc.kotlin:kotlin-compiler-test-extensions -> 0.5.2+2.1.10`
- `com.javiersc.kotlin:kotlin-compiler-gradle-extensions -> 0.5.2+2.1.10`
- `com.javiersc.kotlin:kotlin-compiler-extensions -> 0.5.2+2.1.10`
- `com.javiersc.hubdle:com.javiersc.hubdle.gradle.plugin -> 0.8.2`
- `com.javiersc.hubdle:hubdle-version-catalog -> 0.4.2`
- `org.jetbrains.kotlin:kotlin-gradle-plugin -> 2.1.10`
- `gradle -> 8.12.1`

## [0.13.1+2.1.0] - 2024-11-28

### Updated

- `com.javiersc.hubdle:com.javiersc.hubdle.gradle.plugin -> 0.8.0`
- `com.javiersc.hubdle:hubdle-version-catalog -> 0.4.0`
- `com.javiersc.kotlin:kotlin-compiler-extensions -> 0.5.1+2.1.0`
- `com.javiersc.kotlin:kotlin-compiler-gradle-extensions -> 0.5.1+2.1.0`
- `com.javiersc.kotlin:kotlin-compiler-test-extensions -> 0.5.1+2.1.0`

## [0.13.0+2.1.0] - 2024-11-27

### Updated

- `org.jetbrains.kotlin:kotlin-gradle-plugin -> 2.1.0`
- `com.javiersc.kotlin:kotlin-compiler-test-extensions -> 0.5.0+2.1.0`
- `com.javiersc.kotlin:kotlin-compiler-gradle-extensions -> 0.5.0+2.1.0`
- `com.javiersc.kotlin:kotlin-compiler-extensions -> 0.5.0+2.1.0`
- `gradle -> 8.11.1`
- `com.javiersc.hubdle:com.javiersc.hubdle.gradle.plugin -> 0.7.18`
- `com.javiersc.hubdle:hubdle-version-catalog -> 0.3.13`

## [0.12.0+2.0.21] - 2024-10-10

### Updated

- `com.javiersc.kotlin:kotlin-compiler-test-extensions -> 0.4.4+2.0.21`
- `com.javiersc.kotlin:kotlin-compiler-gradle-extensions -> 0.4.4+2.0.21`
- `com.javiersc.kotlin:kotlin-compiler-extensions -> 0.4.4+2.0.21`
- `com.javiersc.hubdle:hubdle-version-catalog -> 0.3.12`
- `com.javiersc.hubdle:com.javiersc.hubdle.gradle.plugin -> 0.7.14`
- `gradle -> 8.10.2`

## [0.11.0+2.0.20] - 2024-09-08

### Fixed

- `compilerClasspath` not working with the separator `:`

### Updated

- `com.javiersc.kotlin:kotlin-compiler-extensions -> 0.4.3+2.0.20`
- `com.javiersc.kotlin:kotlin-compiler-gradle-extensions -> 0.4.3+2.0.20`
- `com.javiersc.kotlin:kotlin-compiler-test-extensions -> 0.4.3+2.0.20`

## [0.10.0+2.0.20] - 2024-09-02

### Changed

- `kopy-args` to `api` from `implementation` in the Gradle plugin

### Fixed

- Kotlin multiplatform support

## [0.9.0+2.0.20] - 2024-09-01

### Added

- `androidNativeArm32` support
- `androidNativeArm64` support
- `androidNativeX64` support
- `androidNativeX86` support

## [0.8.0+2.0.20] - 2024-08-31

### Fixed

- Kotlin Multiplatform support

## [0.7.0+2.0.20] - 2024-08-29

### Fixed

- crash when atomicfu plugin is applied
- type mismatch errors inside the `copy` function are not shown

### Updated

- `com.javiersc.hubdle:hubdle-version-catalog -> 0.3.7`
- `com.javiersc.hubdle:com.javiersc.hubdle.gradle.plugin -> 0.7.8`

## [0.6.0+2.0.20] - 2024-08-27

### Added

- Kotlin Multiplatform support

## [0.5.0+2.0.20] - 2024-08-26

### Added

- `KopyFunctions` to set the functions that will be generated
- report Kopy uses a different Kotlin version than the project

## [0.4.0+2.0.20] - 2024-08-25

### Added

- `KopyVisibility` to set the visibility of the generated functions

## [0.3.0+2.0.20] - 2024-08-24

### Added

- Atomic library is added automatically to the project when the plugin is applied
- Kopy runtime library is added automatically to the project when the plugin is applied

### Removed

- `Kopyable` interface
- `_initKopyable` function from Kopyable interface
- `getKopyableReference` function from Kopyable interface
- `setKopyableReference` function from Kopyable interface

### Updated

- `com.javiersc.hubdle:hubdle-version-catalog -> 0.3.6`
- `com.javiersc.hubdle:com.javiersc.hubdle.gradle.plugin -> 0.7.7`
- `com.javiersc.kotlin:kotlin-compiler-test-extensions -> 0.3.0+2.0.20`
- `com.javiersc.kotlin:kotlin-compiler-extensions -> 0.3.0+2.0.20`

## [0.2.0+2.0.10] - 2024-08-19

### Added

- `kopy` and `invoke` calls are now marked as errors if the plugin is not applied to the project.

### Updated

- `com.javiersc.hubdle:hubdle-version-catalog -> 0.3.5`
- `com.javiersc.hubdle:com.javiersc.hubdle.gradle.plugin -> 0.7.6`

## [0.1.1+2.0.10] - 2024-08-16

### Updated

- `gradle -> 8.10`
- `com.javiersc.hubdle:hubdle-version-catalog -> 0.3.4`
- `com.javiersc.hubdle:com.javiersc.hubdle.gradle.plugin -> 0.7.4`
- `com.javiersc.kotlin:kotlin-compiler-test-extensions -> 0.2.0+2.0.10`
- `com.javiersc.kotlin:kotlin-compiler-extensions -> 0.2.0+2.0.10`

[Unreleased]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.14.0+2.1.10...HEAD

[0.14.0+2.1.10]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.13.1+2.1.0...0.14.0+2.1.10

[0.13.1+2.1.0]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.13.0+2.1.0...0.13.1+2.1.0

[0.13.0+2.1.0]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.12.0+2.0.21...0.13.0+2.1.0

[0.12.0+2.0.21]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.11.0+2.0.20...0.12.0+2.0.21

[0.11.0+2.0.20]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.10.0+2.0.20...0.11.0+2.0.20

[0.10.0+2.0.20]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.9.0+2.0.20...0.10.0+2.0.20

[0.9.0+2.0.20]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.8.0+2.0.20...0.9.0+2.0.20

[0.8.0+2.0.20]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.7.0+2.0.20...0.8.0+2.0.20

[0.7.0+2.0.20]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.6.0+2.0.20...0.7.0+2.0.20

[0.6.0+2.0.20]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.5.0+2.0.20...0.6.0+2.0.20

[0.5.0+2.0.20]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.4.0+2.0.20...0.5.0+2.0.20

[0.4.0+2.0.20]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.3.0+2.0.20...0.4.0+2.0.20

[0.3.0+2.0.20]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.2.0+2.0.10...0.3.0+2.0.20

[0.2.0+2.0.10]: https://github.com/JavierSegoviaCordoba/kopy/compare/0.1.1+2.0.10...0.2.0+2.0.10

[0.1.1+2.0.10]: https://github.com/JavierSegoviaCordoba/kopy/commits/0.1.1+2.0.10
