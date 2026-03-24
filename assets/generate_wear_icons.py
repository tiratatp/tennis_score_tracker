#!/usr/bin/env python3
"""Generate raster PNG fallback icons for the Wear OS app.

Produces ic_launcher.png (rounded square) and ic_launcher_round.png (circular)
at all 5 Android density buckets, matching the adaptive icon defined in
wear/src/main/res/mipmap-anydpi-v26/.

Reuses the tennis ball drawing logic from generate_feature_graphic.py.
"""

import os
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


def draw_adaptive_icon():
    """Render the full 108x108 adaptive icon (background + foreground) at high res."""
    size = VIEWPORT * RENDER_SCALE  # 432px

    img = Image.new("RGBA", (size, size), NAVY_BG + (255,))
    draw = ImageDraw.Draw(img, "RGBA")

    def s(val):
        """Scale viewport coordinate to render coordinate."""
        return val * RENDER_SCALE

    def sp(x, y):
        """Scale a point from viewport to render coordinates."""
        return (s(x), s(y))

    # Tennis ball body (centered at 54,54 radius 26)
    cx, cy, r = s(54), s(54), s(26)
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=SKY_BLUE + (255,))

    # Seam stroke width: 2 viewport units scaled
    stroke_w = max(1, 2 * RENDER_SCALE)

    # Left seam: M44,28 C36,36 36,46 36,54 C36,62 36,72 44,80
    left_pts = cubic_bezier(
        sp(44, 28), sp(36, 36), sp(36, 46), sp(36, 54),
    ) + cubic_bezier(
        sp(36, 54), sp(36, 62), sp(36, 72), sp(44, 80),
    )

    # Right seam: M64,28 C72,36 72,46 72,54 C72,62 72,72 64,80
    right_pts = cubic_bezier(
        sp(64, 28), sp(72, 36), sp(72, 46), sp(72, 54),
    ) + cubic_bezier(
        sp(72, 54), sp(72, 62), sp(72, 72), sp(64, 80),
    )

    for pts in (left_pts, right_pts):
        draw.line(pts, fill=WHITE + (255,), width=stroke_w, joint="curve")

    return img


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


def main():
    print("Generating Wear OS launcher icons...")
    full_icon = draw_adaptive_icon()
    safe_icon = crop_safe_zone(full_icon)

    for density, px_size in DENSITIES.items():
        out_dir = PROJECT_DIR / "wear" / "src" / "main" / "res" / f"mipmap-{density}"
        out_dir.mkdir(parents=True, exist_ok=True)

        resized = safe_icon.resize((px_size, px_size), Image.LANCZOS)

        # ic_launcher.png - rounded square
        square = apply_rounded_rect_mask(resized)
        square_path = out_dir / "ic_launcher.png"
        square.save(square_path, "PNG")
        print(f"  {square_path} ({px_size}x{px_size})")

        # ic_launcher_round.png - circular
        circle = apply_circle_mask(resized)
        circle_path = out_dir / "ic_launcher_round.png"
        circle.save(circle_path, "PNG")
        print(f"  {circle_path} ({px_size}x{px_size})")

    print(f"\nGenerated {len(DENSITIES) * 2} icon files.")


if __name__ == "__main__":
    main()
