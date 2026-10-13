#!/usr/bin/env python3
"""Placeholder textures for CityWorld: Zoo, painted from each model's box list.

Every model in src/main/java/.../client/model/ is a list of boxes with a texture offset; this script holds
the same list (keep them in step by hand) and paints each box's six faces in flat, slightly shaded colours
with a simple pattern, plus a 16x16 spawn egg per species. They are stand-ins for Sable's art: the point is
that every face a model draws has paint on it, so nothing renders pink-and-black.

    python3 tools/textures/paint.py          # writes into src/main/resources/assets/cityworld_zoo/textures/

Box: (u, v, w, h, d, name) in the standard Minecraft box net: top at (u+d, v) w x d, bottom at (u+d+w, v)
w x d, then a row at v+d of right (d x h), front (w x h), left (d x h), back (w x h).
"""
import os, random
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(__file__), "..", "..", "src", "main", "resources", "assets", "cityworld_zoo", "textures")

def shade(c, k):
    return tuple(max(0, min(255, int(v * k))) for v in c[:3]) + (255,)

def box_faces(u, v, w, h, d):
    """Rectangles (x0, y0, x1, y1, which) for one box net."""
    return [
        (u + d, v, u + d + w, v + d, "top"),
        (u + d + w, v, u + d + 2 * w, v + d, "bottom"),
        (u, v + d, u + d, v + d + h, "right"),
        (u + d, v + d, u + d + w, v + d + h, "front"),
        (u + d + w, v + d, u + 2 * d + w, v + d + h, "left"),
        (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h, "back"),
    ]

FACE_SHADE = {"top": 1.12, "bottom": 0.7, "right": 0.9, "left": 0.9, "front": 1.0, "back": 0.85}

def paint_box(draw, box, base, pattern, rng, override=None):
    u, v, w, h, d, name = box
    colour = override or base
    for x0, y0, x1, y1, face in box_faces(u, v, w, h, d):
        if x1 <= x0 or y1 <= y0:
            continue
        draw.rectangle([x0, y0, x1 - 1, y1 - 1], fill=shade(colour, FACE_SHADE[face]))
        if pattern and face != "bottom":
            pattern(draw, x0, y0, x1, y1, face, rng, colour)

# --- patterns -------------------------------------------------------------------------------------------
def stripes(colour):
    def p(draw, x0, y0, x1, y1, face, rng, base):
        step = 3 if (x1 - x0) > 6 else 2
        off = rng.randint(0, 1)
        for x in range(x0 + off, x1, step):
            draw.line([(x, y0), (x, y1 - 1)], fill=colour + (255,))
    return p

def spots(colour, density=0.18, size=2):
    def p(draw, x0, y0, x1, y1, face, rng, base):
        for _ in range(int((x1 - x0) * (y1 - y0) * density / (size * size)) + 1):
            x = rng.randint(x0, max(x0, x1 - size)); y = rng.randint(y0, max(y0, y1 - size))
            draw.rectangle([x, y, min(x1 - 1, x + size - 1), min(y1 - 1, y + size - 1)], fill=colour + (255,))
    return p

def speckle(colour, density=0.08):
    return spots(colour, density, 1)

# --- species ---------------------------------------------------------------------------------------------
# Each entry: texture size, base colour, pattern, boxes, and per-box colour overrides by name.
SPECIES = {
    "giraffe": dict(size=(128, 128), base=(222, 184, 110), pattern=spots((150, 96, 40), 0.3, 3), boxes=[
        (0, 0, 5, 5, 7, "head"), (24, 0, 1, 3, 1, "ossicone"), (30, 0, 2, 2, 1, "ear"),
        (0, 16, 4, 22, 4, "neck"), (32, 16, 10, 22, 10, "body"), (72, 16, 1, 10, 1, "tail"),
        (0, 48, 4, 26, 4, "front_leg"), (16, 48, 4, 22, 4, "hind_leg")],
        override={"ossicone": (90, 60, 30), "tail": (120, 80, 40)}, egg=((222, 184, 110), (150, 96, 40))),
    "deer": dict(size=(64, 64), base=(140, 96, 58), pattern=speckle((200, 170, 130), 0.06), boxes=[
        (0, 0, 4, 4, 6, "head"), (20, 0, 2, 3, 1, "ear"), (26, 0, 2, 3, 1, "ear"), (32, 0, 1, 5, 1, "antler"), (36, 0, 1, 3, 1, "tine"),
        (0, 12, 3, 8, 4, "neck"), (20, 12, 6, 16, 7, "body"), (52, 12, 2, 4, 1, "tail"),
        (0, 26, 2, 12, 2, "front_leg"), (0, 42, 2, 12, 2, "hind_leg")],
        override={"antler": (110, 90, 70), "tine": (110, 90, 70), "tail": (230, 220, 200)}, egg=((140, 96, 58), (230, 220, 200))),
    "elephant": dict(size=(128, 64), base=(128, 126, 130), pattern=speckle((100, 98, 102), 0.05), boxes=[
        (0, 0, 10, 10, 10, "head"), (40, 0, 1, 10, 8, "ear"), (58, 0, 1, 10, 8, "ear"),
        (76, 0, 3, 6, 3, "trunk"), (88, 0, 3, 6, 3, "trunk"), (100, 0, 3, 6, 3, "trunk"),
        (76, 10, 1, 1, 6, "tusk"), (90, 10, 1, 1, 6, "tusk"),
        (0, 20, 16, 28, 14, "body"), (60, 20, 6, 14, 6, "front_leg"), (84, 20, 6, 14, 6, "hind_leg"), (60, 40, 1, 12, 1, "tail")],
        override={"tusk": (240, 232, 210)}, egg=((128, 126, 130), (240, 232, 210))),
    "rhesus_macaque": dict(size=(64, 32), base=(150, 120, 80), pattern=speckle((120, 95, 60), 0.08), boxes=[
        (0, 0, 4, 4, 4, "head"), (16, 0, 2, 2, 1, "muzzle"), (22, 0, 1, 2, 1, "ear"),
        (0, 8, 5, 9, 5, "body"), (20, 8, 2, 6, 2, "front_leg"), (28, 8, 2, 6, 2, "hind_leg"),
        (36, 8, 1, 6, 1, "tail"), (40, 8, 1, 6, 1, "tail")],
        override={"muzzle": (220, 150, 140), "ear": (220, 150, 140)}, egg=((150, 120, 80), (220, 150, 140))),
    # The horse net (AbstractEquineModel): body first, the neck sits in the body's empty corner.
    "zebra": dict(size=(64, 64), base=(240, 240, 236), pattern=stripes((20, 20, 24)), boxes=[
        (0, 32, 10, 10, 22, "body"), (0, 35, 4, 12, 7, "neck"), (0, 13, 6, 5, 7, "head"), (0, 25, 4, 5, 5, "upper_mouth"),
        (56, 36, 2, 16, 2, "mane"), (48, 21, 4, 11, 4, "leg"), (42, 36, 3, 14, 4, "tail"), (19, 16, 2, 3, 1, "ear")],
        override={"mane": (30, 30, 34), "upper_mouth": (60, 60, 64)}, egg=((240, 240, 236), (20, 20, 24))),
    # The foal: vanilla's BabyHorseModel net (its own texture, like a vanilla foal's).
    "zebra_baby": dict(size=(64, 64), base=(240, 240, 236), pattern=stripes((20, 20, 24)), boxes=[
        (0, 13, 8, 7, 14, "body"), (24, 34, 3, 3, 8, "tail"), (12, 46, 3, 9, 3, "leg"), (0, 46, 3, 9, 3, "leg"),
        (12, 34, 3, 9, 3, "leg"), (0, 34, 3, 9, 3, "leg"), (30, 0, 4, 8, 4, "neck"), (0, 0, 6, 4, 9, "head"),
        (0, 4, 2, 3, 1, "ear"), (0, 0, 2, 3, 1, "ear")],
        override={}, egg=None),
    # The feline net (AdultFelineModel), plus the lion's mane at (40, 12).
    "lion": dict(size=(64, 32), base=(200, 160, 90), pattern=None, boxes=[
        (0, 0, 5, 4, 5, "head"), (0, 24, 3, 2, 2, "nose"), (0, 10, 1, 1, 2, "ear"), (6, 10, 1, 1, 2, "ear"),
        (20, 0, 4, 16, 6, "body"), (0, 15, 1, 8, 1, "tail1"), (4, 15, 1, 8, 1, "tail2"),
        (8, 13, 2, 6, 2, "hind_leg"), (40, 0, 2, 10, 2, "front_leg"), (40, 12, 9, 8, 3, "mane")],
        override={"mane": (120, 70, 30), "nose": (90, 60, 40), "tail2": (90, 60, 40)}, egg=((200, 160, 90), (120, 70, 30))),
    "tiger": dict(size=(64, 32), base=(230, 130, 40), pattern=stripes((25, 20, 20)), boxes=[
        (0, 0, 5, 4, 5, "head"), (0, 24, 3, 2, 2, "nose"), (0, 10, 1, 1, 2, "ear"), (6, 10, 1, 1, 2, "ear"),
        (20, 0, 4, 16, 6, "body"), (0, 15, 1, 8, 1, "tail1"), (4, 15, 1, 8, 1, "tail2"),
        (8, 13, 2, 6, 2, "hind_leg"), (40, 0, 2, 10, 2, "front_leg")],
        override={"nose": (240, 230, 220)}, egg=((230, 130, 40), (25, 20, 20))),
    # The polar bear net (PolarBearModel) in brown.
    "brown_bear": dict(size=(128, 64), base=(110, 72, 40), pattern=speckle((90, 58, 30), 0.1), boxes=[
        (0, 0, 7, 7, 7, "head"), (0, 44, 5, 3, 3, "mouth"), (26, 0, 2, 2, 1, "ear"),
        (0, 19, 14, 14, 11, "body"), (39, 0, 12, 12, 10, "body2"), (50, 22, 4, 10, 8, "hind_leg"), (50, 40, 4, 10, 6, "front_leg")],
        override={"mouth": (140, 100, 70)}, egg=((110, 72, 40), (140, 100, 70))),
}

def paint_species(name, spec):
    rng = random.Random(name)
    im = Image.new("RGBA", spec["size"], (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    for box in spec["boxes"]:
        paint_box(draw, box, spec["base"], spec["pattern"], rng, spec.get("override", {}).get(box[5]))
    path = os.path.join(ROOT, "entity", name + ".png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path)
    return path

def paint_egg(name, base, spot):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    # An egg: a rounded blob with an outline and three spots, like vanilla's.
    draw.ellipse([3, 1, 12, 14], fill=base + (255,), outline=shade(base, 0.55))
    rng = random.Random(name + "egg")
    for _ in range(4):
        x = rng.randint(5, 9); y = rng.randint(4, 11)
        draw.rectangle([x, y, x + 1, y + 1], fill=spot + (255,))
    draw.point((6, 3), fill=shade(base, 1.3))
    path = os.path.join(ROOT, "item", name + "_spawn_egg.png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path)
    return path

# Species with real art (art/<name>/, converted by tools/blockbench/bbmodel_to_java.py): never overpainted.
HANDMADE = {"giraffe", "elephant"}

if __name__ == "__main__":
    for name, spec in SPECIES.items():
        if name in HANDMADE:
            continue
        print(paint_species(name, spec))
        if spec["egg"]:
            print(paint_egg(name, *spec["egg"]))
