# GitHub Actions Workflow - Quick Reference

## Workflow Diagram

```
┌─────────────────────────────────────────┐
│  Trigger: Push to main/master           │
│  OR Manual workflow dispatch            │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  1. Checkout code                       │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  2. Setup JDK 17 & Gradle               │
│     (with caching enabled)              │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  3. Setup Secrets & Credentials         │
│     • Decode keystore (base64)          │
│     • Create publisher credentials JSON │
│     • Inject secrets to gradle.properties│
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  4. Build Release Bundle (AAB)          │
│     ./gradlew androidApp:bundleRelease  │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  5. Publish to Play Store Beta          │
│     ./gradlew androidApp:publishRelease │
│     Bundle (auto-increments version)    │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  6. Upload Build Artifacts              │
│     • Release AAB (30 day retention)    │
│     • Release APK (30 day retention)    │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  7. Commit Version Bump                 │
│     Updates version.properties          │
│     (only if publish succeeded)         │
└─────────────────────────────────────────┘
```

## Required Secrets Checklist

Before running the workflow, ensure all secrets are configured in GitHub:

- [ ] `PLAY_STORE_UPLOAD_KEY` - Base64 encoded keystore
- [ ] `RELEASE_STORE_PASSWORD` - Keystore password  
- [ ] `RELEASE_KEY_PASSWORD` - Key password
- [ ] `PLAY_MANAGED_KEY_ALIAS` - Key alias
- [ ] `PLAY_STORE_CONFIG_JSON` - Service account JSON
- [ ] `MAPBOX_DOWNLOADS_TOKEN` - Mapbox token

**Where to set:** GitHub Repo → Settings → Secrets and variables → Actions

## Quick Start

### Option 1: Automatic Trigger
```bash
# Just push to main/master branch
git push origin main
```

### Option 2: Manual Trigger
1. Go to GitHub repository
2. Click **Actions** tab
3. Select **Build and Publish to Beta**
4. Click **Run workflow**
5. Select branch (usually main)
6. Click **Run workflow** button

## Monitoring

**Check workflow status:**
- GitHub → Actions → [Latest workflow run]

**View logs:**
- Click on workflow run → Click on job name → Expand steps

**Download artifacts:**
- Workflow run → Artifacts section → Download build-outputs.zip

## After Successful Publish

1. **Build number** in `version.properties` is auto-incremented
2. **New release** appears in Google Play Console → Testing → Beta
3. **AAB/APK files** available in GitHub Actions artifacts
4. **Version commit** pushed back to repository

## Troubleshooting Quick Tips

| Error | Solution |
|-------|----------|
| "App not found" | Upload first release manually to Play Console |
| "Invalid credentials" | Check PLAY_STORE_CONFIG_JSON is valid JSON |
| "Keystore error" | Verify PLAY_STORE_UPLOAD_KEY is base64 encoded |
| "Permission denied" | Service account needs publishing permissions |
| "API not enabled" | Enable Google Play Android Developer API |

**For detailed help:** See `.github/WORKFLOW_SETUP.md`

## Build Times

Expected workflow duration: **15-25 minutes**
- Checkout & setup: ~2 min
- Build: ~10-15 min
- Publish: ~3-5 min
- Upload artifacts: ~1-2 min

## Caching

Gradle dependencies are cached to speed up subsequent builds:
- First run: ~20-25 minutes
- Subsequent runs: ~10-15 minutes

## Version Management

```
version.properties
├── appVersion: 0.3 (manually set)
└── buildNumber: 17 (auto-incremented)

Final version: 0.3.17
```

Build number increments automatically after each successful publish.
