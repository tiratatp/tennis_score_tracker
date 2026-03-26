#!/usr/bin/env python3
"""Master script to regenerate ALL icon assets from sport_icons.py.

Generates:
  1. Vector XML foregrounds for phone app (4 themes x 3 sports = 12 files)
  2. Vector XML foregrounds for wear app (1 theme x 3 sports = 3 files)
  3. PNG wear icons (5 densities x 2 shapes x 3 sports = 30 files)
  4. Play Store icons (3 PNG files)

Usage:
    python assets/generate_all_icons.py
"""

import os
import sys
from pathlib import Path

SCRIPT_DIR = Path(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, str(SCRIPT_DIR))
PROJECT_DIR = SCRIPT_DIR.parent

from generate_app_icons import generate_app_icon  # noqa: E402
from generate_wear_icons import generate_wear_icons  # noqa: E402
from sport_icons import FLAVOR_CONFIG, THEME_COLORS, generate_vector_xml  # noqa: E402

FLAVORS = list(FLAVOR_CONFIG.keys())

# Wear app uses only sky_blue theme
WEAR_THEMES = ["sky_blue"]


def generate_phone_vector_xmls():
    """Generate vector XML foregrounds for all phone app flavors and themes."""
    print("Generating phone app vector XMLs...")
    count = 0
    for flavor in FLAVORS:
        for theme_name, color_hex in THEME_COLORS.items():
            xml = generate_vector_xml(flavor, color_hex)
            out_path = (
                PROJECT_DIR / "app" / "src" / flavor / "res" / "drawable"
                / f"ic_launcher_foreground_{theme_name}.xml"
            )
            out_path.parent.mkdir(parents=True, exist_ok=True)
            out_path.write_text(xml)
            print(f"  {out_path}")
            count += 1
    print(f"  Generated {count} vector XML files.\n")


def generate_wear_vector_xmls():
    """Generate vector XML foregrounds for all wear app flavors."""
    print("Generating wear app vector XMLs...")
    count = 0
    for flavor in FLAVORS:
        for theme_name in WEAR_THEMES:
            color_hex = THEME_COLORS[theme_name]
            xml = generate_vector_xml(flavor, color_hex)
            out_path = (
                PROJECT_DIR / "wear" / "src" / flavor / "res" / "drawable"
                / f"ic_launcher_foreground_{theme_name}.xml"
            )
            out_path.parent.mkdir(parents=True, exist_ok=True)
            out_path.write_text(xml)
            print(f"  {out_path}")
            count += 1
    print(f"  Generated {count} vector XML files.\n")


def generate_wear_pngs():
    """Generate PNG wear icons for all flavors."""
    print("Generating wear PNG icons...")
    for flavor in FLAVORS:
        print(f"\n--- {flavor} ---")
        generate_wear_icons(flavor)
    print()


def generate_play_store_icons():
    """Generate Play Store listing icons for all flavors."""
    print("Generating Play Store app icons...")
    for flavor in FLAVORS:
        generate_app_icon(flavor)
    print()


def main():
    generate_phone_vector_xmls()
    generate_wear_vector_xmls()
    generate_wear_pngs()
    generate_play_store_icons()
    print("All icons regenerated successfully.")


if __name__ == "__main__":
    main()
