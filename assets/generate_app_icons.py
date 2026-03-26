#!/usr/bin/env python3
"""Generate 512x512 Play Store listing icons for each sport flavor.

Renders the adaptive icon (background + foreground) with a rounded-square
mask matching the Google Play icon shape.

Usage:
    python assets/generate_app_icons.py              # all flavors
    python assets/generate_app_icons.py tennis       # single flavor
"""

import os
import sys
from pathlib import Path

from PIL import Image

SCRIPT_DIR = Path(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, str(SCRIPT_DIR))
PROJECT_DIR = SCRIPT_DIR.parent

from sport_icons import (  # noqa: E402
    FLAVOR_CONFIG,
    ICON_DRAWERS,
    apply_rounded_rect_mask,
    crop_safe_zone,
)

ICON_SIZE = 512


def generate_app_icon(flavor):
    draw_fn = ICON_DRAWERS[FLAVOR_CONFIG[flavor]]
    full_icon = draw_fn()
    safe_icon = crop_safe_zone(full_icon)
    resized = safe_icon.resize((ICON_SIZE, ICON_SIZE), Image.LANCZOS)
    icon = apply_rounded_rect_mask(resized)

    out_path = (
        PROJECT_DIR / "app" / "src" / flavor / "play"
        / "listings" / "en-US" / "graphics" / "icon" / "icon.png"
    )
    out_path.parent.mkdir(parents=True, exist_ok=True)
    icon.save(out_path, "PNG")
    print(f"Saved {out_path} ({ICON_SIZE}x{ICON_SIZE})")


def main():
    flavors = sys.argv[1:] if len(sys.argv) > 1 else list(FLAVOR_CONFIG.keys())
    for flavor in flavors:
        if flavor not in FLAVOR_CONFIG:
            print(f"Unknown flavor: {flavor}")
            sys.exit(1)

    print("Generating Play Store app icons...")
    for flavor in flavors:
        generate_app_icon(flavor)


if __name__ == "__main__":
    main()
