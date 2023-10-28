# JavierSC Kotlin template

Create libraries for all types of Kotlin projects: android, JVM, Multiplatform, Gradle plugins, and
so on.

## Features

- Easy to publish:
  - Kotlin JVM
  - Kotlin Multiplatform
  - Android library
  - Gradle Plugins
  - Version Catalog
- Versioning based on git tags
- Multiple patching tasks for CHANGELOG
- Autogenerate docs for all projects
- Code analysis with default config
- Code formatter with default config
- Autogenerate and update README badges
- Autogenerate a Version Catalog with all projects in the project
- GitHub Actions:
  - Publish all types of artifacts (MavenCentral and Gradle Plugin Portal)
  - Publish as snapshot any new push to main branch.
  - Show Detekt hints in pull requests (below code that smells)
  - Automatically patch the CHANGELOG and publish all docs to GitHub pages
  - Add new updates to the CHANGELOG when the PR is open by Renovate

## Usage

1. Go to the GitHub Actions tab
2. Select `initial-setup`
3. Press `Run workflow`
4. Fill the workflow form

After that, there will be a new commit modifying/deleting all necessary files.
