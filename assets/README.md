# Assets

Play Store marketing assets for TennisDroid.

## Files

- `app_icon_512.png` — 512x512 app icon (source for the feature graphic)
- `feature_graphic.png` — 1024x500 Play Store feature graphic
- `generate_feature_graphic.py` — Script to regenerate the feature graphic

## Regenerating the Feature Graphic

Requires Python 3 and [Pillow](https://pillow.readthedocs.io/). Uses Roboto font (falls back to Helvetica if unavailable).

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install Pillow
python assets/generate_feature_graphic.py
```

The script reads `app_icon_512.png` and outputs `feature_graphic.png` in the same directory.
