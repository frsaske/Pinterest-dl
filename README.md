# Pinterest Downloader

A standalone Android Pinterest media search and downloader app.

## Automatic APK builds

The repository includes `.github/workflows/android-release.yml`. It runs automatically on every push to any branch and can also be started manually from the **Actions** tab. The workflow:

1. Checks out the pushed source.
2. Installs Java 21 and uses the included Gradle wrapper.
3. Runs unit tests.
4. Builds a signed release APK.
5. Uploads the APK as a workflow artifact.
6. Publishes a GitHub Release named **Pinterest Downloader** with the APK attached.

To move the project to another repository, extract the ZIP so that `build.gradle.kts`, `settings.gradle.kts`, `gradlew`, `app/`, and `.github/` are at the repository root, then commit and push the files. GitHub Actions will detect the workflow and build the app automatically. The destination repository must have Actions enabled and allow workflows to create releases.

For a persistent signing identity, configure repository secrets `KEYSTORE_BASE64`, `STORE_PASSWORD`, and `KEY_PASSWORD`. Without them, the workflow generates a temporary signing key for test releases; those APKs are not suitable for seamless updates over a previously installed signed APK.
