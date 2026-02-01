# Traffix for Android

TraffiX is an app that listens on the local WiFi network for GDL90 data and displays this on a map, as well as retransmitting the data in FLARM format on a UDP socket.

A major use case for TraffiX is to receive GPS and traffic data from a SkyEcho2 Electronic Conspicuity device and relay the data to XCSoar running on the same phone or tablet.
XCSoar is unable to directly receive GDL90 data but most of the functionality of the data can be replicated by converting to FLARM format.

## Building and Publishing

### Automated Publishing to Beta

This project includes a GitHub Actions workflow that automatically builds and publishes the app to Google Play Console's beta track.

**See [.github/WORKFLOW_SETUP.md](.github/WORKFLOW_SETUP.md) for complete setup instructions.**

The workflow runs:
- Automatically on every push to the main/master branch
- Manually via the GitHub Actions tab

### Manual Build

To build locally:

```bash
./gradlew androidApp:assembleRelease
```

To publish to beta (requires credentials setup):

```bash
./gradlew publishToBeta
```

