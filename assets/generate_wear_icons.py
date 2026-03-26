#!/usr/bin/env python3
"""Generate raster PNG fallback icons for the Wear OS app.

Produces ic_launcher.png (rounded square) and ic_launcher_round.png (circular)
at all 5 Android density buckets, matching the adaptive icon defined in
wear/src/{flavor}/res/mipmap-anydpi-v26/.

Usage:
    python assets/generate_wear_icons.py              # all flavors
    python assets/generate_wear_icons.py tennis       # single flavor
    python assets/generate_wear_icons.py badminton pickleball
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
    apply_circle_mask,
    apply_rounded_rect_mask,
    crop_safe_zone,
)

# Output densities: directory suffix -> icon size in px
DENSITIES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}


def generate_wear_icons(flavor):
    """Generate wear icons for the given flavor."""
    draw_fn = ICON_DRAWERS[FLAVOR_CONFIG[flavor]]
    full_icon = draw_fn()
    safe_icon = crop_safe_zone(full_icon)

    for density, px_size in DENSITIES.items():
        out_dir = PROJECT_DIR / "wear" / "src" / flavor / "res" / f"mipmap-{density}"
        out_dir.mkdir(parents=True, exist_ok=True)

        resized = safe_icon.resize((px_size, px_size), Image.LANCZOS)

        square = apply_rounded_rect_mask(resized)
        square_path = out_dir / "ic_launcher.png"
        square.save(square_path, "PNG")
        print(f"  {square_path} ({px_size}x{px_size})")

        circle = apply_circle_mask(resized)
        circle_path = out_dir / "ic_launcher_round.png"
        circle.save(circle_path, "PNG")
        print(f"  {circle_path} ({px_size}x{px_size})")


def main():
    flavors = sys.argv[1:] if len(sys.argv) > 1 else list(FLAVOR_CONFIG.keys())
    for flavor in flavors:
        if flavor not in FLAVOR_CONFIG:
            print(f"Unknown flavor: {flavor}")
            sys.exit(1)

    print("Generating Wear OS launcher icons...")
    for flavor in flavors:
        print(f"\n--- {flavor} ---")
        generate_wear_icons(flavor)

    total = len(flavors) * len(DENSITIES) * 2
    print(f"\nGenerated {total} icon files.")


if __name__ == "__main__":
    main()
