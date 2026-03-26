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

from PIL import Image, ImageDraw

SCRIPT_DIR = Path(os.path.dirname(os.path.abspath(__file__)))
PROJECT_DIR = SCRIPT_DIR.parent

# Colors matching the adaptive icon drawables
SKY_BLUE = (56, 189, 248)  # #38BDF8
WHITE = (255, 255, 255)
NAVY_BG = (12, 45, 72)  # #0C2D48

# Adaptive icon viewport is 108x108; safe zone is center 72x72
VIEWPORT = 108
SAFE_ZONE = 72
SAFE_OFFSET = (VIEWPORT - SAFE_ZONE) // 2  # 18

# Output densities: directory suffix -> icon size in px
DENSITIES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

# Render at this many px per viewport unit for antialiasing
RENDER_SCALE = 4

FLAVOR_CONFIG = {
    "tennis": "draw_tennis_icon",
    "badminton": "draw_badminton_icon",
    "pickleball": "draw_pickleball_icon",
}


def cubic_bezier(p0, p1, p2, p3, steps=64):
    """Evaluate a cubic bezier curve, returning a list of (x, y) points."""
    points = []
    for i in range(steps + 1):
        t = i / steps
        u = 1 - t
        x = u**3 * p0[0] + 3 * u**2 * t * p1[0] + 3 * u * t**2 * p2[0] + t**3 * p3[0]
        y = u**3 * p0[1] + 3 * u**2 * t * p1[1] + 3 * u * t**2 * p2[1] + t**3 * p3[1]
        points.append((x, y))
    return points


def draw_tennis_icon():
    """Render a tennis ball adaptive icon at high res."""
    size = VIEWPORT * RENDER_SCALE

    img = Image.new("RGBA", (size, size), NAVY_BG + (255,))
    draw = ImageDraw.Draw(img, "RGBA")

    def s(val):
        return val * RENDER_SCALE

    def sp(x, y):
        return (s(x), s(y))

    cx, cy, r = s(54), s(54), s(26)
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=SKY_BLUE + (255,))

    stroke_w = max(1, 2 * RENDER_SCALE)

    left_pts = cubic_bezier(
        sp(44, 28), sp(36, 36), sp(36, 46), sp(36, 54),
    ) + cubic_bezier(
        sp(36, 54), sp(36, 62), sp(36, 72), sp(44, 80),
    )

    right_pts = cubic_bezier(
        sp(64, 28), sp(72, 36), sp(72, 46), sp(72, 54),
    ) + cubic_bezier(
        sp(72, 54), sp(72, 62), sp(72, 72), sp(64, 80),
    )

    for pts in (left_pts, right_pts):
        draw.line(pts, fill=WHITE + (255,), width=stroke_w, joint="curve")

    return img


def draw_badminton_icon():
    """Render a shuttlecock adaptive icon at high res."""
    size = VIEWPORT * RENDER_SCALE

    img = Image.new("RGBA", (size, size), NAVY_BG + (255,))
    draw = ImageDraw.Draw(img, "RGBA")

    def s(val):
        return val * RENDER_SCALE

    cx, cy = s(54), s(54)
    r = s(26)
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=SKY_BLUE + (255,))

    # Cork
    cork_r = int(7 * RENDER_SCALE)
    cork_cy = int(cy - 18 * RENDER_SCALE)
    draw.ellipse(
        [cx - cork_r, cork_cy - cork_r, cx + cork_r, cork_cy + cork_r],
        fill=WHITE + (255,),
    )

    # Skirt
    skirt_top = cork_cy + cork_r - int(2 * RENDER_SCALE)
    skirt_bottom = int(cy + 24 * RENDER_SCALE)
    skirt_top_half = int(8 * RENDER_SCALE)
    skirt_bottom_half = int(18 * RENDER_SCALE)
    skirt_points = [
        (cx - skirt_top_half, skirt_top),
        (cx + skirt_top_half, skirt_top),
        (cx + skirt_bottom_half, skirt_bottom),
        (cx - skirt_bottom_half, skirt_bottom),
    ]
    draw.polygon(skirt_points, fill=WHITE + (255,))

    # Feather lines
    stroke_w = max(1, int(1.5 * RENDER_SCALE))
    num_lines = 5
    for i in range(num_lines):
        t = (i + 1) / (num_lines + 1)
        lx = int(cx - skirt_top_half + t * 2 * skirt_top_half)
        bx = int(cx - skirt_bottom_half + t * 2 * skirt_bottom_half)
        draw.line(
            [(lx, skirt_top), (bx, skirt_bottom)],
            fill=SKY_BLUE + (255,), width=stroke_w,
        )

    return img


def draw_pickleball_icon():
    """Render a pickleball (wiffle ball) adaptive icon at high res."""
    size = VIEWPORT * RENDER_SCALE

    img = Image.new("RGBA", (size, size), NAVY_BG + (255,))
    draw = ImageDraw.Draw(img, "RGBA")

    def s(val):
        return val * RENDER_SCALE

    cx, cy = s(54), s(54)
    r = s(26)
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=SKY_BLUE + (255,))

    # Draw holes
    hole_r = int(4 * RENDER_SCALE)
    hole_positions = [
        (0, -16), (-14, -8), (14, -8),
        (-14, 8), (14, 8), (0, 16),
        (0, 0), (-8, -16), (8, -16),
        (-8, 16), (8, 16), (-20, 0), (20, 0),
    ]
    for ox, oy in hole_positions:
        hx = int(cx + ox * RENDER_SCALE)
        hy = int(cy + oy * RENDER_SCALE)
        draw.ellipse(
            [hx - hole_r, hy - hole_r, hx + hole_r, hy + hole_r],
            fill=WHITE + (255,),
        )

    return img


ICON_DRAWERS = {
    "draw_tennis_icon": draw_tennis_icon,
    "draw_badminton_icon": draw_badminton_icon,
    "draw_pickleball_icon": draw_pickleball_icon,
}


def crop_safe_zone(img):
    """Crop the center 72/108 safe zone from the rendered adaptive icon."""
    size = img.width
    offset = int(SAFE_OFFSET / VIEWPORT * size)
    safe_size = int(SAFE_ZONE / VIEWPORT * size)
    return img.crop((offset, offset, offset + safe_size, offset + safe_size))


def apply_rounded_rect_mask(img, radius_fraction=0.18):
    """Apply a rounded rectangle mask. radius_fraction is relative to image size."""
    size = img.width
    radius = int(size * radius_fraction)
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle([0, 0, size - 1, size - 1], radius=radius, fill=255)
    bg = Image.new("RGBA", (size, size), NAVY_BG + (255,))
    bg.paste(img, mask=mask)
    return bg.convert("RGB")


def apply_circle_mask(img):
    """Apply a circular mask."""
    size = img.width
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse([0, 0, size - 1, size - 1], fill=255)
    bg = Image.new("RGBA", (size, size), NAVY_BG + (255,))
    bg.paste(img, mask=mask)
    return bg.convert("RGB")


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
