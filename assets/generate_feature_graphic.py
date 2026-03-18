#!/usr/bin/env python3
"""Generate a 1024x500 Play Store feature graphic for TennisDroid."""

import os
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

# Resolve paths relative to this script's directory
SCRIPT_DIR = Path(os.path.dirname(os.path.abspath(__file__)))

WIDTH, HEIGHT = 1024, 500
SKY_BLUE = (56, 189, 248)  # #38BDF8
WHITE = (255, 255, 255)
NAVY = (12, 45, 72)  # #0C2D48 — matches icon background

img = Image.new("RGB", (WIDTH, HEIGHT), NAVY)
draw = ImageDraw.Draw(img, "RGBA")

# --- App icon (paste the actual 512px icon directly) ---
# Background matches NAVY so the icon blends seamlessly
icon = Image.open(SCRIPT_DIR / "app_icon_512.png").convert("RGB")
icon_size = 440
icon = icon.resize((icon_size, icon_size), Image.LANCZOS)
icon_x = 290 - icon_size // 2
icon_y = HEIGHT // 2 - icon_size // 2
img.paste(icon, (icon_x, icon_y))

# Reinit draw after paste
draw = ImageDraw.Draw(img, "RGBA")

# --- Text on the right side ---
try:
    title_font = ImageFont.truetype("/Library/Fonts/Roboto-Bold.ttf", 72)
    tagline_font = ImageFont.truetype("/Library/Fonts/Roboto-Regular.ttf", 23)
except OSError:
    try:
        title_font = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 72)
        tagline_font = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 23)
    except OSError:
        title_font = ImageFont.load_default()
        tagline_font = ImageFont.load_default()

text_x = 520
title_text = "TennisDroid"
title_bbox = draw.textbbox((0, 0), title_text, font=title_font)
title_h = title_bbox[3] - title_bbox[1]

tagline_line1 = "Smart tennis scoreboard: Bluetooth remote,"
tagline_line2 = "voice announcements & watch sync."
tag1_bbox = draw.textbbox((0, 0), tagline_line1, font=tagline_font)
tag1_h = tag1_bbox[3] - tag1_bbox[1]
tag2_bbox = draw.textbbox((0, 0), tagline_line2, font=tagline_font)
tag2_h = tag2_bbox[3] - tag2_bbox[1]
tagline_h = tag1_h + 6 + tag2_h

# Center all lines vertically as a group
group_h = title_h + 16 + tagline_h
title_y = (HEIGHT - group_h) // 2
tagline_y1 = title_y + title_h + 16
tagline_y2 = tagline_y1 + tag1_h + 6

draw.text((text_x, title_y), title_text, fill=WHITE, font=title_font)
draw.text((text_x, tagline_y1), tagline_line1, fill=SKY_BLUE, font=tagline_font)
draw.text((text_x, tagline_y2), tagline_line2, fill=SKY_BLUE, font=tagline_font)

# --- Decorative vertical line between ball and text ---
line_x = 482
draw.line(
    [(line_x, HEIGHT // 2 - 90), (line_x, HEIGHT // 2 + 90)],
    fill=(56, 189, 248, 50),
    width=2,
)

# --- Save ---
output_path = SCRIPT_DIR / "feature_graphic.png"
img.save(output_path, "PNG")
print(f"Saved {output_path} ({img.size[0]}x{img.size[1]})")
