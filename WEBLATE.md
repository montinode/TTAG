# Weblate Integration Guide

This guide explains how to integrate this Android project with Weblate for translation management.

## Overview

Weblate is a web-based translation tool that helps manage localization of Android string resources. This project includes scripts and configuration to download translations from Weblate.

## Setup Steps

### 1. Create a Weblate Project

1. Go to [Weblate.org](https://weblate.org) or your self-hosted Weblate instance
2. Create a new project (e.g., "TTAG" or "montinode/ttag")
3. Add a component for Android strings:
   - **Name**: android-strings
   - **File format**: Android String Resource
   - **File mask**: `printingSample/src/main/res/values-*/strings.xml`
   - **Monolingual base language file**: `printingSample/src/main/res/values/strings.xml`
   - **Template for new translations**: (leave empty for Android)

### 2. Connect Your Repository

In the Weblate component settings:
- Add your GitHub repository URL: `https://github.com/montinode/TTAG`
- Configure access credentials if the repository is private
- Enable automatic push of translations (optional)

### 3. Get Your API Token

1. Log in to your Weblate account
2. Go to your profile settings
3. Find the "API access" section
4. Copy your API token

### 4. Download Translations

You can download translations using either the Bash script or Python script:

#### Using Python (Recommended - cross-platform)

```bash
# Set your Weblate API token
export WEBLATE_TOKEN='your-api-token-here'

# Download translations from hosted Weblate
python3 download-translations.py

# Or specify custom parameters
python3 download-translations.py \
  --host https://hosted.weblate.org \
  --project montinode/ttag \
  --component android-strings \
  --output-dir printingSample/src/main/res
```

#### Using Bash Script

```bash
# Set your Weblate API token
export WEBLATE_TOKEN='your-api-token-here'

# Download translations
./download-translations.sh

# Or specify custom parameters
./download-translations.sh https://hosted.weblate.org montinode/ttag android-strings
```

### 5. Commit and Build

After downloading translations:

```bash
# Check what was downloaded
git status

# Add the new translation files
git add printingSample/src/main/res/values-*/

# Commit
git commit -m "Update translations from Weblate"

# Build your project
./gradlew build
```

## File Structure

After downloading translations, your project will have the following structure:

```
printingSample/src/main/res/
├── values/
│   └── strings.xml          # English (base language)
├── values-es/
│   └── strings.xml          # Spanish
├── values-fr/
│   └── strings.xml          # French
├── values-de/
│   └── strings.xml          # German
└── values-XX/
    └── strings.xml          # Other languages
```

## Automation with CI/CD

You can integrate the download script into your CI/CD pipeline:

### GitHub Actions Example

```yaml
name: Update Translations

on:
  schedule:
    - cron: '0 0 * * 0'  # Weekly on Sunday
  workflow_dispatch:      # Allow manual trigger

jobs:
  update-translations:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up Python
        uses: actions/setup-python@v2
        with:
          python-version: '3.x'
      
      - name: Download translations
        env:
          WEBLATE_TOKEN: ${{ secrets.WEBLATE_TOKEN }}
        run: python3 download-translations.py
      
      - name: Create Pull Request
        uses: peter-evans/create-pull-request@v3
        with:
          commit-message: Update translations from Weblate
          title: Update translations from Weblate
          branch: update-translations
```

## Configuration Files

- `.weblate` - Weblate configuration file for this project
- `download-translations.sh` - Bash script to download translations
- `download-translations.py` - Python script to download translations (recommended)

## Troubleshooting

### API Token Not Working

- Make sure you've set the `WEBLATE_TOKEN` environment variable
- Verify the token is valid in your Weblate profile settings
- Check that your Weblate project and component names are correct

### Translations Not Appearing

- Ensure translators have completed and saved their work in Weblate
- Check that the language codes match Android's format (e.g., `pt-rBR` for Brazilian Portuguese)
- Verify the file mask in Weblate matches your project structure

### Build Errors After Adding Translations

- Make sure all `strings.xml` files are valid XML
- Check that placeholder formats match across all translations (e.g., `%s`, `%d`)
- Verify that string names are consistent across all language files

## Additional Resources

- [Weblate Documentation](https://docs.weblate.org/)
- [Android String Resources](https://developer.android.com/guide/topics/resources/string-resource)
- [Weblate API Documentation](https://docs.weblate.org/en/latest/api.html)
- [Android Localization Guide](https://developer.android.com/guide/topics/resources/localization)

## Support

For issues with:
- **Weblate setup**: Visit [Weblate Support](https://weblate.org/support/)
- **This project**: Open an issue on [GitHub](https://github.com/montinode/TTAG/issues)
