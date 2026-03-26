"""Shared sport icon drawing code and vector XML generation.

Single source of truth for icon geometry. Provides:
- Pillow drawing functions for raster PNG rendering
- Vector XML generation for Android adaptive icon foregrounds
- Shared constants, bezier utilities, and masking functions
"""

from PIL import Image, ImageDraw

# --- Colors ---
SKY_BLUE = (56, 189, 248)  # #38BDF8
WHITE = (255, 255, 255)
NAVY_BG = (12, 45, 72)  # #0C2D48

THEME_COLORS = {
    "sky_blue": "#38BDF8",
    "grand_slam": "#CCFF00",
    "miami_night": "#00FFFF",
    "colorblind_safe": "#FF6B00",
}

# --- Viewport ---
VIEWPORT = 108
SAFE_ZONE = 72
SAFE_OFFSET = (VIEWPORT - SAFE_ZONE) // 2
RENDER_SCALE = 4

# --- Shared geometry (viewport coordinates) ---
CIRCLE_CX, CIRCLE_CY, CIRCLE_R = 54, 54, 26

# Pickleball hole layout: (cx, cy, rx, ry) in viewport coordinates
# Center holes (larger, round) + edge holes (smaller ellipses for 3D perspective)
PICKLEBALL_HOLES = [
    # Center holes (inverted triangle, round, symmetric about ball center 54,54)
    (54, 44, 5, 5),      # upper center
    (44, 58, 5, 5),      # lower left (54-10)
    (64, 58, 5, 5),      # lower right (54+10)
    # Edge holes (8 at 45° intervals, radius 22 from center, elongated ellipses)
    (54, 32, 3.5, 1.5),  # top (0°) — flat horizontal
    (70, 38, 2.5, 2.5),  # upper-right (45°) — circular
    (76, 54, 1.5, 3.5),  # right (90°) — tall vertical
    (70, 70, 2.5, 2.5),  # lower-right (135°) — circular
    (54, 76, 3.5, 1.5),  # bottom (180°) — flat horizontal
    (38, 70, 2.5, 2.5),  # lower-left (225°) — circular
    (32, 54, 1.5, 3.5),  # left (270°) — tall vertical
    (38, 38, 2.5, 2.5),  # upper-left (315°) — circular
]


# --- Bezier utilities ---

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


def quadratic_bezier(p0, p1, p2, steps=64):
    """Evaluate a quadratic bezier curve, returning a list of (x, y) points."""
    points = []
    for i in range(steps + 1):
        t = i / steps
        u = 1 - t
        x = u**2 * p0[0] + 2 * u * t * p1[0] + t**2 * p2[0]
        y = u**2 * p0[1] + 2 * u * t * p1[1] + t**2 * p2[1]
        points.append((x, y))
    return points


# --- Pillow drawing functions ---

def draw_tennis_icon(bg_color=NAVY_BG):
    """Render a tennis ball adaptive icon at high res.

    Args:
        bg_color: Background color tuple (R, G, B), or None for transparent.
    """
    size = VIEWPORT * RENDER_SCALE
    bg = bg_color + (255,) if bg_color else (0, 0, 0, 0)
    img = Image.new("RGBA", (size, size), bg)
    draw = ImageDraw.Draw(img, "RGBA")

    def s(val):
        return val * RENDER_SCALE

    def sp(x, y):
        return (s(x), s(y))

    cx, cy, r = s(CIRCLE_CX), s(CIRCLE_CY), s(CIRCLE_R)
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


def draw_badminton_icon(bg_color=NAVY_BG):
    """Render a shuttlecock adaptive icon with straight-sided trapezoid skirt.

    Args:
        bg_color: Background color tuple (R, G, B), or None for transparent.
    """
    size = VIEWPORT * RENDER_SCALE
    bg = bg_color + (255,) if bg_color else (0, 0, 0, 0)
    img = Image.new("RGBA", (size, size), bg)
    draw = ImageDraw.Draw(img, "RGBA")

    def s(val):
        return val * RENDER_SCALE

    def sp(x, y):
        return (s(x), s(y))

    # Background circle
    cx, cy, r = s(CIRCLE_CX), s(CIRCLE_CY), s(CIRCLE_R)
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=SKY_BLUE + (255,))

    # Cork (white circle)
    cork_cx, cork_cy, cork_r = s(54), s(37), s(5)
    draw.ellipse(
        [cork_cx - cork_r, cork_cy - cork_r, cork_cx + cork_r, cork_cy + cork_r],
        fill=WHITE + (255,),
    )

    # Straight trapezoid skirt
    skirt_pts = [sp(49, 41), sp(42, 72), sp(66, 72), sp(59, 41)]
    draw.polygon(skirt_pts, fill=WHITE + (255,))

    # Feather separation lines (accent-colored on white skirt)
    stroke_w = max(1, int(1.5 * RENDER_SCALE))

    draw.line([sp(50, 42), sp(44, 72)], fill=SKY_BLUE + (255,), width=stroke_w)
    draw.line([sp(54, 42), sp(54, 72)], fill=SKY_BLUE + (255,), width=stroke_w)
    draw.line([sp(58, 42), sp(64, 72)], fill=SKY_BLUE + (255,), width=stroke_w)

    return img


def draw_pickleball_icon(bg_color=NAVY_BG):
    """Render a pickleball (wiffle ball) adaptive icon at high res.

    Args:
        bg_color: Background color tuple (R, G, B), or None for transparent.
    """
    size = VIEWPORT * RENDER_SCALE
    bg = bg_color + (255,) if bg_color else (0, 0, 0, 0)
    img = Image.new("RGBA", (size, size), bg)
    draw = ImageDraw.Draw(img, "RGBA")

    def s(val):
        return val * RENDER_SCALE

    cx, cy = s(CIRCLE_CX), s(CIRCLE_CY)
    r = s(CIRCLE_R)
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=SKY_BLUE + (255,))

    for hcx, hcy, rx, ry in PICKLEBALL_HOLES:
        x = int(s(hcx))
        y = int(s(hcy))
        srx = int(s(rx))
        sry = int(s(ry))
        draw.ellipse(
            [x - srx, y - sry, x + srx, y + sry],
            fill=WHITE + (255,),
        )

    return img


# --- Flavor mapping ---

FLAVOR_CONFIG = {
    "tennis": "draw_tennis_icon",
    "badminton": "draw_badminton_icon",
    "pickleball": "draw_pickleball_icon",
}

ICON_DRAWERS = {
    "draw_tennis_icon": draw_tennis_icon,
    "draw_badminton_icon": draw_badminton_icon,
    "draw_pickleball_icon": draw_pickleball_icon,
}


# --- Utility functions ---

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


def draw_sport_ball(flavor, size):
    """Draw a sport ball with transparent background at the given size.

    Renders the adaptive icon on a transparent background, crops the safe zone,
    trims to the content bounding box (square-centered), and resizes.
    Used by feature graphic generation.
    """
    draw_fn = ICON_DRAWERS[FLAVOR_CONFIG[flavor]]
    full_icon = draw_fn(bg_color=None)
    safe_icon = crop_safe_zone(full_icon)
    bbox = safe_icon.getbbox()
    if bbox:
        x0, y0, x1, y1 = bbox
        w, h = x1 - x0, y1 - y0
        max_dim = max(w, h)
        cx, cy = (x0 + x1) // 2, (y0 + y1) // 2
        x0 = cx - max_dim // 2
        y0 = cy - max_dim // 2
        x1 = x0 + max_dim
        y1 = y0 + max_dim
        safe_icon = safe_icon.crop((x0, y0, x1, y1))
    return safe_icon.resize((size, size), Image.LANCZOS)


# --- Vector XML generation ---

def _circle_path(cx, cy, r):
    """Generate SVG path data for a circle."""
    d = r * 2
    return f"M{cx},{cy}m-{r},0a{r},{r} 0,1 1,{d} 0a{r},{r} 0,1 1,-{d} 0"


def generate_vector_xml(sport, color_hex):
    """Generate Android vector drawable XML for the given sport and accent color."""
    generators = {
        "tennis": _tennis_vector_xml,
        "badminton": _badminton_vector_xml,
        "pickleball": _pickleball_vector_xml,
    }
    return generators[sport](color_hex)


def _tennis_vector_xml(color_hex):
    ball = _circle_path(CIRCLE_CX, CIRCLE_CY, CIRCLE_R)
    return f"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">

    <!-- Tennis ball body -->
    <path
        android:fillColor="{color_hex}"
        android:pathData="{ball}" />

    <!-- Tennis ball seam - left curve -->
    <path
        android:fillColor="#00000000"
        android:strokeColor="#FFFFFF"
        android:strokeWidth="2"
        android:pathData="M44,28C36,36 36,46 36,54C36,62 36,72 44,80" />

    <!-- Tennis ball seam - right curve -->
    <path
        android:fillColor="#00000000"
        android:strokeColor="#FFFFFF"
        android:strokeWidth="2"
        android:pathData="M64,28C72,36 72,46 72,54C72,62 72,72 64,80" />
</vector>
"""


def _badminton_vector_xml(color_hex):
    bg_circle = _circle_path(CIRCLE_CX, CIRCLE_CY, CIRCLE_R)
    cork = _circle_path(54, 37, 5)
    return f"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">

    <!-- Background circle -->
    <path
        android:fillColor="{color_hex}"
        android:pathData="{bg_circle}" />

    <!-- Shuttlecock cork -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="{cork}" />

    <!-- Shuttlecock skirt (trapezoid) -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M49,41 L42,72 L66,72 L59,41Z" />

    <!-- Feather separation lines -->
    <path
        android:fillColor="#00000000"
        android:strokeColor="{color_hex}"
        android:strokeWidth="1.5"
        android:pathData="M50,42 L44,72" />
    <path
        android:fillColor="#00000000"
        android:strokeColor="{color_hex}"
        android:strokeWidth="1.5"
        android:pathData="M54,42 L54,72" />
    <path
        android:fillColor="#00000000"
        android:strokeColor="{color_hex}"
        android:strokeWidth="1.5"
        android:pathData="M58,42 L64,72" />
</vector>
"""


def _ellipse_path(cx, cy, rx, ry):
    """Generate SVG path data for an ellipse."""
    return (
        f"M{cx},{cy}m-{rx},0"
        f"a{rx},{ry} 0,1 1,{rx * 2},0"
        f"a{rx},{ry} 0,1 1,-{rx * 2},0"
    )


def _pickleball_vector_xml(color_hex):
    ball = _circle_path(CIRCLE_CX, CIRCLE_CY, CIRCLE_R)
    hole_lines = "\n".join(
        f'    <path android:fillColor="#FFFFFF" '
        f'android:pathData="{_ellipse_path(hcx, hcy, rx, ry)}" />'
        for hcx, hcy, rx, ry in PICKLEBALL_HOLES
    )
    return f"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">

    <!-- Pickleball body -->
    <path
        android:fillColor="{color_hex}"
        android:pathData="{ball}" />

    <!-- Holes pattern -->
{hole_lines}
</vector>
"""
