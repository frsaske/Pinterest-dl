# Pinterest-dl

## Automated Android releases

Every push to `main` and every manual workflow run executes unit tests, builds the release APK, uploads it as a workflow artifact, and publishes it to a GitHub Release.

For a persistent signing identity, configure repository secrets `KEYSTORE_BASE64`, `STORE_PASSWORD`, and `KEY_PASSWORD`. Without them, the workflow uses a temporary keystore for test releases.
