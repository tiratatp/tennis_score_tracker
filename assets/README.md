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
  generate_app_icons.py
  generate_feature_graphic.py
  generate_wear_icons.py
```

The `play/` directories follow the `gradle-play-publisher` source set convention
(`src/{flavor}/play/`).

## Regenerating App Icons (Play Store)

```bash
# All flavors
python generate_app_icons.py

# Single flavor
python generate_app_icons.py tennis
```

Outputs 512x512 PNG icons to `app/src/{flavor}/play/listings/en-US/graphics/icon/`.

## Regenerating Feature Graphics

Requires Python 3 and [Pillow](https://pillow.readthedocs.io/). Uses Roboto font (falls back to Helvetica if unavailable).

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install Pillow

# All flavors
python generate_feature_graphic.py

# Single flavor
python generate_feature_graphic.py tennis
```

The script reads screenshots from `screenshots/{flavor}/` and outputs to
`app/src/{flavor}/play/listings/en-US/graphics/feature-graphic/`.

## Regenerating Wear Icons

```bash
# All flavors
python generate_wear_icons.py

# Single flavor
python generate_wear_icons.py tennis
```

Outputs raster PNG icons to `wear/src/{flavor}/res/mipmap-{density}/`.
