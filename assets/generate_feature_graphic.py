#!/usr/bin/env python3
"""Generate a 1024x500 Play Store feature graphic for TennisDroid.

Layout: left side has app icon + title + feature bullets,
right side has a landscape screenshot in a device mockup frame.
"""

import math
import os
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

# Resolve paths relative to this script's directory
SCRIPT_DIR = Path(os.path.dirname(os.path.abspath(__file__)))
PROJECT_DIR = SCRIPT_DIR.parent

WIDTH, HEIGHT = 1024, 500
SKY_BLUE = (56, 189, 248)  # #38BDF8
WHITE = (255, 255, 255)
NAVY_DARK = (8, 32, 56)  # darker left edge
NAVY_LIGHT = (18, 58, 90)  # lighter right edge
BEZEL_COLOR = (40, 40, 40)  # dark gray device frame
BEZEL_INNER = (20, 20, 20)  # slightly darker inner edge


def lerp_color(c1, c2, t):
    """Linearly interpolate between two RGB colors."""
    return tuple(int(a + (b - a) * t) for a, b in zip(c1, c2))


def draw_gradient(img, c1, c2):
    """Draw a left-to-right horizontal gradient."""
    draw = ImageDraw.Draw(img)
    for x in range(img.width):
        t = x / (img.width - 1)
        color = lerp_color(c1, c2, t)
        draw.line([(x, 0), (x, img.height)], fill=color)


def draw_rounded_rect(draw, bbox, radius, fill=None, outline=None, width=1):
    """Draw a rounded rectangle (compatible with older Pillow versions)."""
    x0, y0, x1, y1 = bbox
    if fill:
        # Fill the rounded rectangle using overlapping rectangles + circles
        draw.rectangle([x0 + radius, y0, x1 - radius, y1], fill=fill)
        draw.rectangle([x0, y0 + radius, x1, y1 - radius], fill=fill)
        draw.pieslice([x0, y0, x0 + 2 * radius, y0 + 2 * radius], 180, 270, fill=fill)
        draw.pieslice(
            [x1 - 2 * radius, y0, x1, y0 + 2 * radius], 270, 360, fill=fill
        )
        draw.pieslice(
            [x0, y1 - 2 * radius, x0 + 2 * radius, y1], 90, 180, fill=fill
        )
        draw.pieslice(
            [x1 - 2 * radius, y1 - 2 * radius, x1, y1], 0, 90, fill=fill
        )
    if outline:
        # Draw the outline arcs and lines
        draw.arc(
            [x0, y0, x0 + 2 * radius, y0 + 2 * radius],
            180,
            270,
            fill=outline,
            width=width,
        )
        draw.arc(
            [x1 - 2 * radius, y0, x1, y0 + 2 * radius],
            270,
            360,
            fill=outline,
            width=width,
        )
        draw.arc(
            [x0, y1 - 2 * radius, x0 + 2 * radius, y1],
            90,
            180,
            fill=outline,
            width=width,
        )
        draw.arc(
            [x1 - 2 * radius, y1 - 2 * radius, x1, y1],
            0,
            90,
            fill=outline,
            width=width,
        )
        draw.line(
            [(x0 + radius, y0), (x1 - radius, y0)], fill=outline, width=width
        )
        draw.line(
            [(x0 + radius, y1), (x1 - radius, y1)], fill=outline, width=width
        )
        draw.line(
            [(x0, y0 + radius), (x0, y1 - radius)], fill=outline, width=width
        )
        draw.line(
            [(x1, y0 + radius), (x1, y1 - radius)], fill=outline, width=width
        )


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


def draw_tennis_ball(size):
    """Draw a tennis ball at the given size with transparent background.

    Based on the Android vector drawable: a sky-blue circle with two white
    seam curves. The ball fills the entire output image (no viewport padding).
    """
    # Render at 4x for antialiasing, then downscale
    render_size = size * 4

    # In the original 108-unit viewport, the ball is centered at (54,54)
    # with radius 26, so it spans from 28 to 80 (diameter 52).
    # We remap all coordinates so the ball fills the canvas:
    #   new_coord = (old_coord - 28) / 52 * render_size
    def remap(x, y):
        return ((x - 28) / 52 * render_size, (y - 28) / 52 * render_size)

    ball_img = Image.new("RGBA", (render_size, render_size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(ball_img, "RGBA")

    # Ball body: fills the canvas
    draw.ellipse([0, 0, render_size - 1, render_size - 1], fill=SKY_BLUE + (255,))

    # Seam curves (white, stroke width 2 in 108-unit space -> scaled)
    stroke_w = max(1, int(2 / 52 * render_size))

    # Left seam: M44,28 C36,36 36,46 36,54 C36,62 36,72 44,80
    left_pts = cubic_bezier(
        remap(44, 28), remap(36, 36), remap(36, 46), remap(36, 54),
    ) + cubic_bezier(
        remap(36, 54), remap(36, 62), remap(36, 72), remap(44, 80),
    )

    # Right seam: M64,28 C72,36 72,46 72,54 C72,62 72,72 64,80
    right_pts = cubic_bezier(
        remap(64, 28), remap(72, 36), remap(72, 46), remap(72, 54),
    ) + cubic_bezier(
        remap(72, 54), remap(72, 62), remap(72, 72), remap(64, 80),
    )

    for pts in (left_pts, right_pts):
        draw.line(pts, fill=(255, 255, 255, 255), width=stroke_w, joint="curve")

    # Downscale with antialiasing
    ball_img = ball_img.resize((size, size), Image.LANCZOS)
    return ball_img


def load_font(path, size):
    """Try to load a font, falling back gracefully."""
    try:
        return ImageFont.truetype(path, size)
    except OSError:
        return None


def get_fonts():
    """Load Roboto fonts with fallbacks."""
    roboto_bold_paths = [
        "/Library/Fonts/Roboto-Bold.ttf",
        "/usr/share/fonts/truetype/roboto/Roboto-Bold.ttf",
    ]
    roboto_regular_paths = [
        "/Library/Fonts/Roboto-Regular.ttf",
        "/usr/share/fonts/truetype/roboto/Roboto-Regular.ttf",
    ]
    helvetica = "/System/Library/Fonts/Helvetica.ttc"

    title_font = None
    feature_font = None

    for path in roboto_bold_paths:
        title_font = load_font(path, 58)
        if title_font:
            break

    for path in roboto_regular_paths:
        feature_font = load_font(path, 24)
        if feature_font:
            break

    if not title_font:
        title_font = load_font(helvetica, 58) or ImageFont.load_default()
    if not feature_font:
        feature_font = load_font(helvetica, 24) or ImageFont.load_default()

    return title_font, feature_font


def main():
    # --- Background gradient ---
    img = Image.new("RGB", (WIDTH, HEIGHT))
    draw_gradient(img, NAVY_DARK, NAVY_LIGHT)

    draw = ImageDraw.Draw(img, "RGBA")
    title_font, feature_font = get_fonts()

    # --- Layout constants ---
    left_margin = 60
    right_section_x = 460  # where the device mockup starts

    # --- Measure title to size icon accordingly ---
    title_text = "TennisDroid"
    title_bbox = draw.textbbox((0, 0), title_text, font=title_font)
    title_h = title_bbox[3] - title_bbox[1]
    title_top_offset = title_bbox[1]  # offset from y=0 to first pixel

    # Icon at ~112% of title cap height (150% * 0.75)
    icon_size = int(title_h * 1.125)
    icon_gap = 20

    # Vertical position for the title+icon row
    row_y = 120

    # --- App icon (tennis ball drawn from vector paths) ---
    # Center icon and title on the same vertical midline
    row_mid = row_y + icon_size // 2
    icon = draw_tennis_ball(icon_size)
    img.paste(icon, (left_margin, row_mid - icon_size // 2), icon)
    draw = ImageDraw.Draw(img, "RGBA")  # reinit after paste

    # --- App name (vertically centered with icon) ---
    title_x = left_margin + icon_size + icon_gap
    title_y = row_mid - title_h // 2 - title_top_offset
    draw.text((title_x, title_y), title_text, fill=WHITE, font=title_font)

    # --- Feature bullet points ---
    features = [
        "Bluetooth remote control",
        "Voice score announcements",
        "Wear OS watch sync",
    ]
    bullet_y = row_y + icon_size + 40
    bullet_spacing = 42
    dot_radius = 5

    for i, feature in enumerate(features):
        y = bullet_y + i * bullet_spacing
        # Draw bullet dot, left-aligned with icon
        dot_cx = left_margin + dot_radius
        dot_cy = y + 12  # vertically center with text
        draw.ellipse(
            [dot_cx - dot_radius, dot_cy - dot_radius,
             dot_cx + dot_radius, dot_cy + dot_radius],
            fill=SKY_BLUE,
        )
        # Draw feature text after dot
        draw.text((left_margin + dot_radius * 2 + 10, y), feature, fill=SKY_BLUE, font=feature_font)

    # --- Device mockup with screenshot (right side) ---
    screenshot_path = PROJECT_DIR / "screenshots" / "score-landscape.png"
    screenshot = Image.open(screenshot_path).convert("RGB")

    # Target area for the screenshot inside the bezel
    bezel_thickness = 8
    corner_radius = 18
    screen_w = 500
    screen_h = int(screen_w * screenshot.height / screenshot.width)

    # Resize screenshot to fit
    screenshot = screenshot.resize((screen_w, screen_h), Image.LANCZOS)

    # Device frame outer dimensions
    device_w = screen_w + 2 * bezel_thickness
    device_h = screen_h + 2 * bezel_thickness

    # Position: centered vertically, right-aligned with margin
    device_x = right_section_x + (WIDTH - right_section_x - device_w) // 2
    device_y = (HEIGHT - device_h) // 2

    # Draw the outer bezel (dark gray rounded rectangle)
    draw_rounded_rect(
        draw,
        [device_x, device_y, device_x + device_w, device_y + device_h],
        corner_radius,
        fill=BEZEL_COLOR,
    )

    # Draw inner darker edge
    draw_rounded_rect(
        draw,
        [device_x + 2, device_y + 2, device_x + device_w - 2, device_y + device_h - 2],
        corner_radius - 2,
        fill=BEZEL_INNER,
    )

    # Paste the screenshot inside the bezel
    screen_x = device_x + bezel_thickness
    screen_y = device_y + bezel_thickness
    img.paste(screenshot, (screen_x, screen_y))

    # Draw a subtle highlight on top edge of bezel for depth
    draw = ImageDraw.Draw(img, "RGBA")
    draw_rounded_rect(
        draw,
        [device_x, device_y, device_x + device_w, device_y + device_h],
        corner_radius,
        outline=(255, 255, 255, 40),
        width=1,
    )

    # --- Save ---
    output_path = SCRIPT_DIR / "feature_graphic.png"
    img.save(output_path, "PNG")
    print(f"Saved {output_path} ({img.size[0]}x{img.size[1]})")


if __name__ == "__main__":
    main()
