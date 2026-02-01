# Quick Start: Downloading Translations from Weblate

This guide provides a quick reference for downloading translations from Weblate.

## Prerequisites

1. A Weblate account at https://hosted.weblate.org (or your own Weblate instance)
2. Your project set up on Weblate with the component `android-strings`
3. Your Weblate API token (available from your profile settings)

## Step 1: Set Your API Token

```bash
export WEBLATE_TOKEN='your-api-token-here'
```

To make this permanent, add it to your `~/.bashrc` or `~/.zshrc`:

```bash
echo "export WEBLATE_TOKEN='your-api-token-here'" >> ~/.bashrc
source ~/.bashrc
```

## Step 2: Download Translations

### Using Python (Recommended)

```bash
python3 download-translations.py
```

### Using Bash

```bash
./download-translations.sh
```

## Step 3: Verify Downloaded Files

```bash
# List all translation files
find printingSample/src/main/res -name "strings.xml" -type f

# Check a specific language
cat printingSample/src/main/res/values-es/strings.xml
```

## Step 4: Build and Test

```bash
# Build the Android project
./gradlew build

# Or if using Android Studio, just rebuild the project
```

## Customization

If your Weblate setup uses different project/component names:

```bash
# Python
python3 download-translations.py \
  --project your-project-name \
  --component your-component-name

# Bash
./download-translations.sh https://hosted.weblate.org your-project-name your-component-name
```

## Troubleshooting

### "WEBLATE_TOKEN not set" Error

Make sure you've exported the token in your current shell session:

```bash
export WEBLATE_TOKEN='your-token'
```

### No Translations Downloaded

1. Check that your project and component names are correct
2. Verify translations have been completed in Weblate
3. Ensure your API token has read permissions

### API Errors

Check the Weblate API documentation: https://docs.weblate.org/en/latest/api.html

## Next Steps

- See [WEBLATE.md](WEBLATE.md) for detailed setup instructions
- Set up automated translation downloads in CI/CD
- Configure Weblate to automatically push translations to your repository

## Support

- Weblate Documentation: https://docs.weblate.org/
- Android Localization: https://developer.android.com/guide/topics/resources/localization
