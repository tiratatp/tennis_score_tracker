# Assets

Play Store marketing assets for TennisDroid, BadmintonDroid, and PickleballDroid.

## Structure

```
app/src/
  tennis/play/listings/en-US/       # Tennis Play Store metadata
    title.txt
    short-description.txt
    full-description.txt
    graphics/feature-graphic/       # Generated
  badminton/play/listings/en-US/    # Badminton Play Store metadata
    ...
  pickleball/play/listings/en-US/   # Pickleball Play Store metadata
    ...

assets/
  sport_icons.py              # Single source of truth for icon geometry
  generate_all_icons.py       # Master script: regenerates ALL icon assets
  generate_app_icons.py       # Play Store listing icons
  generate_feature_graphic.py # Play Store feature graphics
  generate_wear_icons.py      # Wear OS fallback PNG icons
```

`sport_icons.py` is the single source of truth for all icon geometry. It provides:
- Pillow drawing functions for raster PNG rendering
- Vector XML generation for Android adaptive icon foregrounds
- Shared constants, bezier utilities, and masking functions

The `play/` directories follow the `gradle-play-publisher` source set convention
(`src/{flavor}/play/`).

## Regenerating All Icons

The recommended way to regenerate all icon assets at once:

```bash
python3 generate_all_icons.py
```

This generates:
- Vector XML foregrounds for phone app (4 themes x 3 sports = 12 files)
- Vector XML foregrounds for wear app (1 theme x 3 sports = 3 files)
- PNG wear icons (5 densities x 2 shapes x 3 sports = 30 files)
- Play Store listing icons (3 files)

## Regenerating Individual Assets

### App Icons (Play Store)

```bash
python3 generate_app_icons.py              # all flavors
python3 generate_app_icons.py tennis       # single flavor
```

Outputs 512x512 PNG icons to `app/src/{flavor}/play/listings/en-US/graphics/icon/`.

### Feature Graphics

Requires Python 3 and [Pillow](https://pillow.readthedocs.io/). Uses Roboto font (falls back to Helvetica if unavailable).

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install Pillow

python3 generate_feature_graphic.py              # all flavors
python3 generate_feature_graphic.py tennis       # single flavor
```

The script reads screenshots from `screenshots/{flavor}/` and outputs to
`app/src/{flavor}/play/listings/en-US/graphics/feature-graphic/`.

### Wear Icons

```bash
python3 generate_wear_icons.py              # all flavors
python3 generate_wear_icons.py tennis       # single flavor
```

Outputs raster PNG icons to `wear/src/{flavor}/res/mipmap-{density}/`.
