#!/usr/bin/env python3
"""
Script to download translations from Weblate for Android projects.
Usage: python3 download-translations.py [--host HOST] [--project PROJECT] [--component COMPONENT]
"""

import os
import sys
import json
import argparse
import urllib.request
import urllib.error

def download_translations(host, project, component, token, output_dir):
    """Download translations from Weblate."""
    
    if not token:
        print("ERROR: WEBLATE_TOKEN environment variable is not set.")
        print("\nTo use this script, you need to:")
        print("1. Set up your project on Weblate (https://weblate.org)")
        print("2. Get an API token from your Weblate profile")
        print("3. Set it as: export WEBLATE_TOKEN='your-token'")
        print("\nExample usage with token:")
        print("export WEBLATE_TOKEN='your-token'")
        print("python3 download-translations.py")
        sys.exit(1)
    
    print(f"Downloading translations from Weblate...")
    print(f"Host: {host}")
    print(f"Project: {project}")
    print(f"Component: {component}")
    print()
    
    # Create output directory if it doesn't exist
    os.makedirs(output_dir, exist_ok=True)
    
    # Fetch list of available translations
    api_url = f"{host}/api/components/{project}/{component}/translations/"
    print(f"Fetching available translations from: {api_url}")
    
    try:
        req = urllib.request.Request(api_url)
        req.add_header('Authorization', f'Token {token}')
        
        with urllib.request.urlopen(req) as response:
            data = json.loads(response.read().decode())
            translations = data.get('results', [])
    except urllib.error.URLError as e:
        print(f"Error fetching translations: {e}")
        sys.exit(1)
    
    if not translations:
        print("No translations found or API error.")
        sys.exit(1)
    
    # Download each translation
    for translation in translations:
        lang = translation.get('language_code')
        if not lang:
            continue
        
        # Convert language code to Android format (e.g., pt-BR -> pt-rBR, pt-br -> pt-rBR)
        if '-' in lang:
            parts = lang.split('-')
            if len(parts) == 2:
                # Always uppercase the region code for Android
                android_lang = f"{parts[0]}-r{parts[1].upper()}"
            else:
                android_lang = lang
        else:
            android_lang = lang
        
        # Determine output directory
        if lang == 'en':
            lang_dir = os.path.join(output_dir, 'values')
        else:
            lang_dir = os.path.join(output_dir, f'values-{android_lang}')
        
        # Create directory
        os.makedirs(lang_dir, exist_ok=True)
        
        # Download translation file
        output_file = os.path.join(lang_dir, 'strings.xml')
        download_url = f"{host}/api/translations/{project}/{component}/{lang}/file/?format=aresource"
        
        print(f"Downloading {lang} -> {output_file}")
        
        try:
            req = urllib.request.Request(download_url)
            req.add_header('Authorization', f'Token {token}')
            
            with urllib.request.urlopen(req) as response:
                content = response.read()
                with open(output_file, 'wb') as f:
                    f.write(content)
        except urllib.error.URLError as e:
            print(f"  Error downloading {lang}: {e}")
            continue
    
    print()
    print("Download complete!")
    print(f"Translations saved to: {output_dir}")

def main():
    parser = argparse.ArgumentParser(
        description='Download translations from Weblate for Android projects'
    )
    parser.add_argument(
        '--host',
        default='https://hosted.weblate.org',
        help='Weblate host URL (default: https://hosted.weblate.org)'
    )
    parser.add_argument(
        '--project',
        default='montinode/ttag',
        help='Weblate project identifier (default: montinode/ttag)'
    )
    parser.add_argument(
        '--component',
        default='android-strings',
        help='Weblate component identifier (default: android-strings)'
    )
    parser.add_argument(
        '--output-dir',
        default='printingSample/src/main/res',
        help='Output directory for translations (default: printingSample/src/main/res)'
    )
    
    args = parser.parse_args()
    token = os.environ.get('WEBLATE_TOKEN')
    
    download_translations(
        args.host,
        args.project,
        args.component,
        token,
        args.output_dir
    )

if __name__ == '__main__':
    main()
