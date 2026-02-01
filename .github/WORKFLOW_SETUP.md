# GitHub Actions Workflow Setup

This document explains how to configure the GitHub Actions workflow for building and publishing the TraffiX Android app to Google Play Beta.

## Workflow Overview

The workflow (`publish-beta.yml`) automatically:
1. Builds the Android app release bundle (`.aab`)
2. Publishes it to Google Play Console's beta track
3. Increments the build number
4. Uploads build artifacts for review

## Triggers

The workflow runs on:
- **Push to main/master branch**: Automatically builds and publishes on every merge to main
- **Manual dispatch**: Can be triggered manually from the GitHub Actions tab

## Required GitHub Secrets

You must configure the following secrets in your GitHub repository settings (Settings → Secrets and variables → Actions → New repository secret):

### 1. PLAY_STORE_UPLOAD_KEY
The Android app signing keystore file, base64 encoded.

**To create:**
```bash
# If you have a keystore file (e.g., release-keystore.jks)
base64 -i release-keystore.jks -o keystore-base64.txt
# Copy the contents of keystore-base64.txt and paste as the secret value
```

### 2. RELEASE_STORE_PASSWORD
The password for the keystore file.

### 3. RELEASE_KEY_PASSWORD
The password for the signing key within the keystore.

### 4. PLAY_MANAGED_KEY_ALIAS
The alias of the signing key within the keystore.

### 5. PLAY_STORE_CONFIG_JSON
The Google Play Service Account JSON credentials file.

**To create:**
1. Go to Google Play Console → Setup → API access
2. Create a new service account or use an existing one
3. Grant the service account permissions (Admin or specific app permissions)
4. Create a JSON key for the service account
5. Copy the entire contents of the JSON file and paste as the secret value

### 6. MAPBOX_DOWNLOADS_TOKEN
Your Mapbox SDK download token (required for building with Mapbox dependencies).

**To get:**
1. Log in to your Mapbox account
2. Go to Account → Tokens
3. Create a secret token with downloads:read scope
4. Copy the token value

## Setting Up Secrets

1. Go to your GitHub repository
2. Click on **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret** for each secret listed above
4. Enter the exact secret name (case-sensitive)
5. Paste the secret value
6. Click **Add secret**

## Google Play Console Setup

Before the workflow can publish to Google Play, you need to:

1. **Create the app** in Google Play Console (if not already done)
2. **Upload a manual release first** to initialize the app
3. **Create a service account** with publishing permissions
4. **Enable the Google Play Android Developer API** in Google Cloud Console
5. **Grant the service account permissions** to your app

## Testing the Workflow

1. **Manual Test**: Go to Actions → Build and Publish to Beta → Run workflow
2. **Automatic Test**: Push a commit to the main branch
3. Monitor the workflow execution in the Actions tab
4. Check Google Play Console → Testing → Closed testing → Beta for the new release

## Workflow Steps Explained

1. **Checkout code**: Gets the latest code from the repository
2. **Set up JDK 17**: Installs Java 17 (required for Android builds)
3. **Setup Gradle**: Configures Gradle with caching for faster builds
4. **Decode keystore**: Converts base64-encoded keystore to file
5. **Setup publisher credentials**: Creates the service account JSON file
6. **Create gradle.properties**: Adds secrets to gradle.properties file
7. **Grant execute permission**: Makes gradlew executable
8. **Build release bundle**: Compiles and bundles the release AAB
9. **Publish to Google Play Beta**: Uploads to Play Store beta track
10. **Upload artifacts**: Saves AAB/APK files for download
11. **Commit version bump**: Updates version.properties with new build number

## Build Artifacts

After each workflow run, you can download:
- Release AAB file (uploaded to Play Store)
- Release APK file (for testing/distribution)

Find artifacts in: Actions → [workflow run] → Artifacts section

## Troubleshooting

### Common Issues

**"App not found" error:**
- Ensure the app exists in Google Play Console
- Verify the applicationId matches in both places
- Upload a manual release first to initialize the app

**"Invalid credentials" error:**
- Check that PLAY_STORE_CONFIG_JSON is valid JSON
- Verify the service account has correct permissions
- Enable Google Play Android Developer API

**Build failures:**
- Check the workflow logs for specific errors
- Verify all secrets are set correctly
- Ensure gradle.properties has all required properties

**Keystore errors:**
- Verify PLAY_STORE_UPLOAD_KEY is properly base64 encoded
- Check passwords are correct
- Confirm key alias matches

## Manual Publishing (Without Workflow)

If you need to publish manually:

```bash
# Build the release bundle
./gradlew androidApp:bundleRelease

# Publish to beta (requires local setup of credentials)
./gradlew publishToBeta
```

## Version Management

The workflow automatically increments the build number in `version.properties` after each successful publish. This is handled by the `incBuild` Gradle task.

## Security Notes

- Never commit keystore files or credentials to the repository
- All secrets are stored securely in GitHub Secrets
- The workflow creates temporary files that are automatically cleaned up
- Service account JSON should have minimal required permissions
