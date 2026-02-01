#!/bin/bash
# Script to download translations from Weblate
# Usage: ./download-translations.sh [WEBLATE_HOST] [PROJECT] [COMPONENT]

set -e

# Configuration
WEBLATE_HOST="${1:-https://hosted.weblate.org}"
PROJECT="${2:-montinode/ttag}"
COMPONENT="${3:-android-strings}"
OUTPUT_DIR="printingSample/src/main/res"

echo "Downloading translations from Weblate..."
echo "Host: $WEBLATE_HOST"
echo "Project: $PROJECT"
echo "Component: $COMPONENT"
echo ""

# Create output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Note: This script requires curl and optionally jq for JSON parsing
# For actual download, you would need:
# 1. A Weblate API token (set as WEBLATE_TOKEN environment variable)
# 2. The project to be set up on Weblate

if [ -z "$WEBLATE_TOKEN" ]; then
    echo "WARNING: WEBLATE_TOKEN environment variable is not set."
    echo "To use this script, you need to:"
    echo "1. Set up your project on Weblate"
    echo "2. Get an API token from your Weblate profile"
    echo "3. Set it as: export WEBLATE_TOKEN='your-token'"
    echo ""
    echo "Example usage with token:"
    echo "export WEBLATE_TOKEN='your-token'"
    echo "./download-translations.sh"
    exit 1
fi

# Fetch list of available translations
echo "Fetching available translations..."
# Note: This uses grep/cut for JSON parsing to avoid external dependencies.
# If the API response format changes, consider using 'jq' for more robust parsing:
# TRANSLATIONS=$(curl -s -H "Authorization: Token $WEBLATE_TOKEN" \
#     "$WEBLATE_HOST/api/components/$PROJECT/$COMPONENT/translations/" | jq -r '.results[].language_code')
TRANSLATIONS=$(curl -s -H "Authorization: Token $WEBLATE_TOKEN" \
    "$WEBLATE_HOST/api/components/$PROJECT/$COMPONENT/translations/" \
    | grep -o '"language_code":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TRANSLATIONS" ]; then
    echo "No translations found or API error."
    exit 1
fi

# Download each translation
for lang in $TRANSLATIONS; do
    # Convert language code to Android format (e.g., pt-BR -> pt-rBR)
    # First convert to uppercase after hyphen, then add 'r' prefix
    android_lang=$(echo "$lang" | awk -F'-' '{if (NF==2) print $1"-r"toupper($2); else print $0}')
    
    # Determine output directory
    if [ "$lang" = "en" ]; then
        dir="$OUTPUT_DIR/values"
    else
        dir="$OUTPUT_DIR/values-$android_lang"
    fi
    
    # Create directory
    mkdir -p "$dir"
    
    # Download translation file
    echo "Downloading $lang -> $dir/strings.xml"
    curl -s -H "Authorization: Token $WEBLATE_TOKEN" \
        "$WEBLATE_HOST/api/translations/$PROJECT/$COMPONENT/$lang/file/?format=aresource" \
        -o "$dir/strings.xml"
done

echo ""
echo "Download complete!"
echo "Translations saved to: $OUTPUT_DIR"
