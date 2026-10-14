#!/usr/bin/env python3
"""Turn a Blockbench free-format model (per-face UVs) into a vanilla Java entity mesh plus a repacked texture.

Vanilla cubes take ONE texture offset per box and lay the six faces out in a fixed net (ModelPart.Cube), so a
model painted with per-face UVs cannot be used as-is. This script:

  1. reads the .bbmodel and its texture;
  2. gives every element a box net at the same pixels-per-unit the artist used (2 px/unit for a 512 texture
     on a ~64-unit animal), packing the nets into a new texture and copying each painted face across;
  3. writes the Java `mesh()` body for the part hierarchy named in RIGS below (pivots for animation), and
  4. writes a box-UV .bbmodel beside it, so later edits happen on the repacked version and round-trip through
     Blockbench's own Java export.

Coordinates: Blockbench shows the model as it renders. Vanilla renders model space through scale(-1,-1,1),
so java = (-x, 24 - y, z) and rotations (-rx, -ry, rz). Faces: a side face's net cell takes the Blockbench
face unchanged (java WEST is the model's -x side, which is Blockbench's +x "east" face, and so on); the top
cell takes Blockbench's "up" face turned 180 degrees and the bottom cell its "down" face flipped
horizontally, which is how Blockbench's own box-UV template lays them out.

    python3 tools/blockbench/bbmodel_to_java.py giraffe <model.bbmodel> [texture.png]   # else the embedded one
    python3 tools/blockbench/bbmodel_to_java.py zebra <same model> zebra_bite.png --texture-as=zebra_bite   # a skin
"""
import base64, io, json, math, os, sys, uuid
from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..", "..")

# --- rigs -------------------------------------------------------------------------------------------------
# A rig names the Java parts, their pivots (Blockbench coordinates), their parents, and which elements belong
# to each (by element name and the index of the Blockbench top-level group that holds them). Elements with
# their own rotation become a child part of the part they are assigned to, pivoted where Blockbench says
# (or at "pivot_override" when the artist's pivot is somewhere odd).
RIGS = {
    "giraffe": {
        "texture_scale": 2,            # px per model unit in the artist's texture
        "parts": [
            # name, parent, bb pivot, [(group index, element name), ...]
            ("body", None, (0, 28, 2), [(0, "Torso"), (0, "Pale belly")]),
            ("neck", None, (0, 29, -8), [(5, "Long neck"), (5, "Light throat"), (5, "Short mane")]),
            ("head", "neck", (0, 54.5, -9), [(5, "Head"), (5, "Muzzle"), (5, "Lower jaw"), (5, "Ossicone"), (5, "Ossicone cap")]),
            ("left_ear", "head", None, [(5, "Ear", 0)]),
            ("right_ear", "head", None, [(5, "Ear", 1)]),
            ("left_front_leg", None, (-4.7, 30, -6.5), [(1, "Shoulder"), (1, "Upper leg")]),
            ("left_front_lower", "left_front_leg", (-4.7, 14, -6.5), [(1, "Lower leg")]),
            ("left_front_hoof", "left_front_lower", (-4.7, 3.5, -6.5), [(1, "Hoof")]),
            ("left_hind_leg", None, (-4.7, 30, 10.5), [(2, "Haunch"), (2, "Upper leg")]),
            ("left_hind_lower", "left_hind_leg", (-4.7, 14, 10.5), [(2, "Lower leg")]),
            ("left_hind_hoof", "left_hind_lower", (-4.7, 3.5, 10.5), [(2, "Hoof")]),
            ("right_front_leg", None, (4.7, 30, -6.5), [(3, "Shoulder"), (3, "Upper leg")]),
            ("right_front_lower", "right_front_leg", (4.7, 14, -6.5), [(3, "Lower leg")]),
            ("right_front_hoof", "right_front_lower", (4.7, 3.5, -6.5), [(3, "Hoof")]),
            ("right_hind_leg", None, (4.7, 30, 10.5), [(4, "Haunch"), (4, "Upper leg")]),
            ("right_hind_lower", "right_hind_leg", (4.7, 14, 10.5), [(4, "Lower leg")]),
            ("right_hind_hoof", "right_hind_lower", (4.7, 3.5, 10.5), [(4, "Hoof")]),
            ("tail", None, (0, 31, 12), [(6, "Tail stem"), (6, "Tail tuft")]),
        ],
        # A rotated element's pivot, when the artist's is somewhere unhelpful: the ears were rotated into place
        # about a point far below them, so for wiggling they pivot where they meet the head instead.
        "pivot_override": {"left_ear": "nearest:0,58,-9", "right_ear": "nearest:0,58,-9"},
    },
    # Sable's deer (2026-10-13), reshaped from the spec_to_model export: upright neck, two-box head, legs with
    # the knee pivot set in Blockbench. Elements are picked by name within their top-level group; the two
    # "Head" boxes are the skull and the muzzle.
    "deer": {
        "parts": [
            ("body", None, (0, 16, 1), [(0, "Body")]),
            ("neck", None, (0, 16, -5), [(1, "Neck")]),
            ("head", "neck", (0, 26, -4), [(1, "Head", 0), (1, "Head", 1), (1, "Left antler"), (1, "Right antler"),
                                           (1, "Left ear"), (1, "Right ear"), (1, "Left tine"), (1, "Right tine")]),
            ("left_front_leg", None, (-2, 12, -5), [(2, "Left front upper")]),
            ("left_front_lower", "left_front_leg", (-2, 7, -5), [(2, "Left front lower")]),
            ("right_front_leg", None, (2, 12, -5), [(3, "Right front upper")]),
            ("right_front_lower", "right_front_leg", (2, 7, -5), [(3, "Right front lower")]),
            ("left_hind_leg", None, (-2, 12, 7), [(4, "Left hind upper")]),
            ("left_hind_lower", "left_hind_leg", (-2, 7, 7), [(4, "Left hind lower")]),
            ("right_hind_leg", None, (2, 12, 7), [(5, "Right hind upper")]),
            ("right_hind_lower", "right_hind_leg", (2, 7, 7), [(5, "Right hind lower")]),
            ("tail", None, (0, 17, 9), [(6, "Tail")]),
        ],
    },
    # Sable's lion (2026-10-13), free format: one-box legs split at y=5 for a knee, the muzzle (rotated) becomes
    # its own part under the head, the mane and face are the head.
    "lion": {
        "split_y": {(1, "Paw and leg"): 5, (2, "Paw and leg"): 5, (3, "Paw and leg"): 5, (4, "Paw and leg"): 5},
        "parts": [
            ("body", None, (0, 14, 1), [(0, "Torso"), (0, "Shoulders")]),
            ("head", "body", (0, 14, -9), [(5, "Square mane"), (5, "Face"), (5, "Left ear"), (5, "Right ear"), (6, "Muzzle")]),
            ("left_front_leg", None, (-3.5, 11, -6.5), [(1, "Paw and leg upper")]),
            ("left_front_lower", "left_front_leg", (-3.5, 5, -6.5), [(1, "Paw and leg lower")]),
            ("left_hind_leg", None, (-3.5, 11, 9), [(2, "Paw and leg upper")]),
            ("left_hind_lower", "left_hind_leg", (-3.5, 5, 9), [(2, "Paw and leg lower")]),
            ("right_front_leg", None, (3.5, 11, -6.5), [(3, "Paw and leg upper")]),
            ("right_front_lower", "right_front_leg", (3.5, 5, -6.5), [(3, "Paw and leg lower")]),
            ("right_hind_leg", None, (3.5, 11, 9), [(4, "Paw and leg upper")]),
            ("right_hind_lower", "right_hind_leg", (3.5, 5, 9), [(4, "Paw and leg lower")]),
            ("tail", "body", (0, 16, 11), [(7, "Tail shaft"), (7, "Dark tail tuft")]),
        ],
    },
    # Sable's tiger (2026-10-13), free format: 17-unit legs split at y=8, the rotated cheek ruffs, muzzle and
    # whiskers become parts under the head, the tail tip its own part on the tail.
    "tiger": {
        "split_y": {(1, "Leg and paw"): 8, (2, "Leg and paw"): 8, (3, "Leg and paw"): 8, (4, "Leg and paw"): 8},
        "parts": [
            ("body", None, (0, 16, 1), [(0, "Torso"), (0, "Broad shoulders")]),
            ("head", "body", (0, 17, -10), [(5, "Head"), (5, "Cheek ruff", 0), (5, "Cheek ruff", 1), (6, "Ear"), (7, "Ear"), (8, "Muzzle")]),
            # Each side's two whiskers are a part at their root on the muzzle, so they can twitch.
            ("left_whiskers", "head", (-2.6, 14.7, -18.4), [(11, "Whisker", 0), (11, "Whisker", 1)]),
            ("right_whiskers", "head", (2.4, 14.7, -18.4), [(12, "Whisker", 0), (12, "Whisker", 1)]),
            ("left_front_leg", None, (-4.2, 16, -7.5), [(1, "Leg and paw upper")]),
            ("left_front_lower", "left_front_leg", (-4.2, 8, -7.5), [(1, "Leg and paw lower")]),
            ("left_hind_leg", None, (-4.2, 16, 9), [(2, "Leg and paw upper")]),
            ("left_hind_lower", "left_hind_leg", (-4.2, 8, 9), [(2, "Leg and paw lower")]),
            ("right_front_leg", None, (4.2, 16, -7.5), [(3, "Leg and paw upper")]),
            ("right_front_lower", "right_front_leg", (4.2, 8, -7.5), [(3, "Leg and paw lower")]),
            ("right_hind_leg", None, (4.2, 16, 10), [(4, "Leg and paw upper")]),
            ("right_hind_lower", "right_hind_leg", (4.2, 8, 10), [(4, "Leg and paw lower")]),
            ("tail", "body", (0, 18, 12), [(9, "Tail shaft")]),
            ("tail_tip", "tail", (0, 10, 14), [(10, "Tail tip")]),
        ],
        # The tail tip was modelled six units behind the shaft; moved to grow out of the shaft's lower back.
        "offset": {(10, "Tail tip"): (0, 0, -7)},
    },
    # Sable's macaque: the file's groups are the rig; head and tail ride on the body so a sit carries them.
    "rhesus_macaque": {"reparent": {"head": "body", "tail": "body"}},
    "elephant": {
        # Sable's pillar legs are one 19-unit element each; split at y=9 into a 10-unit upper and a 9-unit
        # lower so the knee can bend, each half reading its own rows of the painted leg.
        "split_y": {(1, "Pillar leg"): 9, (2, "Pillar leg"): 9, (3, "Pillar leg"): 9, (4, "Pillar leg"): 9},
        # The legs' outer faces lie exactly on the body's side and front planes where they overlap it, and
        # two faces on one plane flicker (z-fighting). A tenth of a unit in on x and z is invisible and ends it.
        "inset_xz": {(1, "Pillar leg"): 0.1, (2, "Pillar leg"): 0.1, (3, "Pillar leg"): 0.1, (4, "Pillar leg"): 0.1},
        "parts": [
            ("body", None, (0, 24, 0), [(0, "Deep rectangular torso")]),
            ("head", "body", (0, 27, -11), [(5, "Sloped forehead"), (5, "Cheeks")]),
            ("left_ear", "head", (-5.1, 28.5, -11.5), [(6, "Broad ear")]),
            ("right_ear", "head", (5.1, 28.5, -11.5), [(7, "Broad ear")]),
            ("trunk", "head", (0, 23, -18), [(8, "Upper trunk")]),
            ("trunk_tip", "trunk", (0, 13.5, -18.5), [(9, "Bent trunk tip")]),
            ("left_tusk", "head", (-5.1, 20, -17), [(10, "Tusk root"), (10, "Forward tusk")]),
            ("right_tusk", "head", (5.1, 20, -17), [(11, "Tusk root"), (11, "Forward tusk")]),
            ("left_front_leg", None, (-4, 19, -7.5), [(1, "Pillar leg upper")]),
            ("left_front_lower", "left_front_leg", (-4, 9, -7.5), [(1, "Pillar leg lower")]),
            ("left_hind_leg", None, (-4, 19, 11.5), [(2, "Pillar leg upper")]),
            ("left_hind_lower", "left_hind_leg", (-4, 9, 11.5), [(2, "Pillar leg lower")]),
            ("right_front_leg", None, (4, 19, -7.5), [(3, "Pillar leg upper")]),
            ("right_front_lower", "right_front_leg", (4, 9, -7.5), [(3, "Pillar leg lower")]),
            ("right_hind_leg", None, (4, 19, 11.5), [(4, "Pillar leg upper")]),
            ("right_hind_lower", "right_hind_leg", (4, 9, 11.5), [(4, "Pillar leg lower")]),
            ("tail", "body", (0, 31, 12), [(12, "Short tail"), (12, "Tail tip")]),
        ],
    },
}

# --- maths ---------------------------------------------------------------------------------------------
def rot_matrix(rx, ry, rz):
    """Rotation Z*Y*X (X applied first), degrees, as rows."""
    cx, sx = math.cos(math.radians(rx)), math.sin(math.radians(rx))
    cy, sy = math.cos(math.radians(ry)), math.sin(math.radians(ry))
    cz, sz = math.cos(math.radians(rz)), math.sin(math.radians(rz))
    Rx = [[1, 0, 0], [0, cx, -sx], [0, sx, cx]]
    Ry = [[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]]
    Rz = [[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]]
    return mul(Rz, mul(Ry, Rx))

def mul(a, b):
    return [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]

def apply(m, v):
    return [sum(m[i][k] * v[k] for k in range(3)) for i in range(3)]

def transpose(m):
    return [[m[j][i] for j in range(3)] for i in range(3)]

def sub(a, b): return [a[i] - b[i] for i in range(3)]
def add(a, b): return [a[i] + b[i] for i in range(3)]

def to_java(p):
    return [-p[0], 24 - p[1], p[2]]

def to_java_local(p):
    return [-p[0], -p[1], p[2]]

def fmt(f):
    s = f"{f:.3f}".rstrip("0").rstrip(".")
    if "." not in s: s += ".0"
    return s + "F"

def measure_scale(d, tex):
    """Texture pixels per model unit, the median over every painted side face."""
    ratios = []
    px_per_uv = tex.width / d["resolution"]["width"]
    for e in d["elements"]:
        w, h, dep = [e["to"][i] - e["from"][i] for i in range(3)]
        for face, fw, fh in (("north", w, h), ("south", w, h), ("east", dep, h), ("west", dep, h)):
            f = e["faces"].get(face)
            if f and fw > 0 and fh > 0:
                x1, y1, x2, y2 = f["uv"]
                ratios.append(abs(x2 - x1) * px_per_uv / fw)
                ratios.append(abs(y2 - y1) * px_per_uv / fh)
    ratios.sort()
    r = ratios[len(ratios) // 2] if ratios else 1.0
    return max(1, int(round(r)))

def split_element(e, y_cut, overlap=0.5):
    """Three elements from one, cut horizontally at y_cut: the upper keeps the up face, the lower the down face,
    the side faces are sliced by height, and the two new inside faces borrow the rows beside the cut. Both
    halves keep the full width, so the sides meet flush (a narrower half left a visible step at the elephant's
    knee); a third, slightly narrower "plug" rides with the lower half {overlap} units up inside the upper,
    hidden until the knee folds, so the gap that opens there shows skin rather than daylight. Its sides sit
    inside the upper's, so nothing shares a plane and nothing flickers. The plug and the two cut faces are
    painted a shade darker, so the wedge that opens on the outside of a fold reads as a crease in the skin."""
    y0, y1 = e["from"][1], e["to"][1]
    f_up = (y1 - y_cut) / (y1 - y0)                 # share of the height above the cut
    f_plug = (y1 - y_cut - overlap) / (y1 - y0)     # the plug's top, inside the upper
    upper = json.loads(json.dumps(e)); lower = json.loads(json.dumps(e)); plug = json.loads(json.dumps(e))
    upper["name"] += " upper"; lower["name"] += " lower"; plug["name"] += " lower plug"
    upper["uuid"] += "-upper"; lower["uuid"] += "-lower"; plug["uuid"] += "-plug"
    upper["from"] = [e["from"][0], y_cut, e["from"][2]]
    lower["to"] = [e["to"][0], y_cut, e["to"][2]]
    eps = 0.03
    plug["from"] = [e["from"][0] + eps, y_cut - 0.02, e["from"][2] + eps]
    plug["to"] = [e["to"][0] - eps, y_cut + overlap, e["to"][2] - eps]
    for face in ("north", "south", "east", "west"):
        f = e["faces"].get(face)
        if not f:
            continue
        x1, v1, x2, v2 = f["uv"]       # uv[1] is the face's top edge, uv[3] its bottom, whichever is larger
        vc = v1 + (v2 - v1) * f_up
        upper["faces"][face] = dict(f, uv=[x1, v1, x2, vc])
        lower["faces"][face] = dict(f, uv=[x1, vc, x2, v2])
        plug["faces"][face] = dict(f, uv=[x1, v1 + (v2 - v1) * f_plug, x2, vc], shade=0.85)
    n = e["faces"].get("north")
    if n:
        x1, v1, x2, v2 = n["uv"]
        vc = v1 + (v2 - v1) * f_up
        dep_share = (e["to"][2] - e["from"][2]) / (y1 - y0) * (v2 - v1)
        upper["faces"]["down"] = dict(n, uv=[x1, vc - dep_share, x2, vc], shade=0.85)
        lower["faces"]["up"] = dict(n, uv=[x1, vc, x2, vc + dep_share], shade=0.85)
        plug["faces"]["up"] = dict(n, uv=[x1, vc - dep_share, x2, vc], shade=0.85)
        plug["faces"]["down"] = dict(n, uv=[x1, vc, x2, vc + dep_share], shade=0.85)
    else:
        upper["faces"].pop("down", None); lower["faces"].pop("up", None)
    return upper, lower, plug

# --- the work ----------------------------------------------------------------------------------------------
def convert(rig_name, bb_path, tex_path, texture_as=None):
    rig = RIGS.get(rig_name, {})
    d = json.load(open(bb_path))
    if tex_path:
        tex = Image.open(tex_path).convert("RGBA")
    else:
        src = d["textures"][0]["source"]
        tex = Image.open(io.BytesIO(base64.b64decode(src.split(",", 1)[1]))).convert("RGBA")
    scale = rig.get("texture_scale") or measure_scale(d, tex)
    elements = {e["uuid"]: e for e in d["elements"]}
    groups = []  # top-level groups as lists of element dicts, in order, nested groups flattened into them
    def collect(node, into):
        if isinstance(node, dict):
            for c in node.get("children", []):
                collect(c, into)
        else:
            into.append(elements[node])
    for node in d["outliner"]:
        found = []
        collect(node, found)
        groups.append(found)
    for (gi, ename), inset in rig.get("inset_xz", {}).items():
        for e in groups[gi]:
            if e["name"] == ename:
                e["from"] = [e["from"][0] + inset, e["from"][1], e["from"][2] + inset]
                e["to"] = [e["to"][0] - inset, e["to"][1], e["to"][2] - inset]
    for (gi, ename), (dx, dy, dz) in rig.get("offset", {}).items():
        for e in groups[gi]:
            if e["name"] == ename:
                e["from"] = [e["from"][0] + dx, e["from"][1] + dy, e["from"][2] + dz]
                e["to"] = [e["to"][0] + dx, e["to"][1] + dy, e["to"][2] + dz]
    for (gi, ename), y_cut in rig.get("split_y", {}).items():
        for e in list(groups[gi]):
            if e["name"] == ename:
                groups[gi].remove(e)
                groups[gi].extend(split_element(e, y_cut))   # upper, lower, and the lower's plug

    # Assign elements to parts: from the rig table, or, when the file's groups are named (a model that went
    # out through spec_to_model.py and came back), straight from the groups: each group is a part with its
    # origin, rotation and nesting, holding its direct elements.
    parts = {}
    order = []
    if "parts" not in rig:
        def from_group(node, parent):
            name = node["name"]
            parts[name] = {"parent": parent, "pivot": node.get("origin", [0, 0, 0]), "rotation": tuple(node.get("rotation") or [0, 0, 0]),
                           "elements": [elements[c] for c in node.get("children", []) if isinstance(c, str)]}
            order.append(name)
            for c in node.get("children", []):
                if isinstance(c, dict):
                    from_group(c, name)
        for node in d["outliner"]:
            if isinstance(node, dict) and node.get("name"):
                from_group(node, None)
        for child, parent in rig.get("reparent", {}).items():
            parts[child]["parent"] = parent
        rig = dict(rig, parts=[])
    for name, parent, pivot, members in rig["parts"]:
        found = []
        for m in members:
            gi, ename = m[0], m[1]
            nth = m[2] if len(m) > 2 else None
            matches = [e for e in groups[gi] if e["name"] == ename]
            if nth is not None:
                matches = [matches[nth]]
            if ename.endswith(" lower"):
                matches += [e for e in groups[gi] if e["name"] == ename + " plug"]
            if not matches:
                raise SystemExit(f"rig {rig_name}: no element '{ename}' in group {gi}")
            found.extend(matches)
        parts[name] = {"parent": parent, "pivot": pivot, "elements": found, "rotation": (0, 0, 0)}
        order.append(name)

    # A rotated element becomes its own part (pivot = its origin, or the override), unless the part holds
    # only that element, in which case the part itself takes the rotation.
    boxes = []  # (part name, element, local from, local to)
    for name in list(order):
        p = parts[name]
        rots = {(tuple(e.get("rotation") or [0, 0, 0]), tuple(e.get("origin") or [0, 0, 0])) for e in p["elements"]}
        if len(rots) == 1 and any(next(iter(rots))[0]) and len(p["elements"]) > 1:
            rot, origin = next(iter(rots))
            p["rotation"], p["pivot"] = rot, list(origin)
            R = rot_matrix(*rot)
            for e in p["elements"]:
                boxes.append((name, e, sub(e["from"], origin), sub(e["to"], origin)))
            continue
        for e in p["elements"]:
            rot = e.get("rotation") or [0, 0, 0]
            if any(rot):
                origin = e.get("origin", [0, 0, 0])
                R = rot_matrix(*rot)
                ov = rig.get("pivot_override", {}).get(name)
                if ov and ov.startswith("nearest:"):
                    target = [float(x) for x in ov.split(":")[1].split(",")]
                    corners = [[e[k][i] for i, k in enumerate(c)] for c in
                               [("from", "from", "from"), ("to", "from", "from"), ("from", "to", "from"), ("to", "to", "from"),
                                ("from", "from", "to"), ("to", "from", "to"), ("from", "to", "to"), ("to", "to", "to")]]
                    world = [add(origin, apply(R, sub(c, origin))) for c in corners]
                    pivot = min(world, key=lambda w: sum((w[i] - target[i]) ** 2 for i in range(3)))
                else:
                    pivot = origin
                # local box relative to the chosen pivot, in the element's own rotated frame
                shift = apply(transpose(R), sub(pivot, origin))
                lf = sub(sub(e["from"], origin), shift)
                lt = sub(sub(e["to"], origin), shift)
                if len(p["elements"]) == 1:
                    p["pivot"] = pivot
                    p["rotation"] = tuple(rot)
                    boxes.append((name, e, lf, lt))
                else:
                    sub_name = base = name + "_" + e["name"].lower().replace(" ", "_")
                    n = 2
                    while sub_name in parts:   # two rotated "Cheek ruff" elements: ruff, ruff2
                        sub_name = f"{base}{n}"; n += 1
                    parts[sub_name] = {"parent": name, "pivot": pivot, "elements": [e], "rotation": tuple(rot)}
                    order.append(sub_name)
                    boxes.append((sub_name, e, lf, lt))
            else:
                boxes.append((name, e, sub(e["from"], p["pivot"]), sub(e["to"], p["pivot"])))

    # Pack a box net per element: cell sizes in model units, placed at integer unit offsets.
    # A box's UV net is laid out by the game from its width, height and depth, so a fractional size puts a
    # face edge at a fractional texel and the sampler leaks the neighbouring face's row across it (the
    # elephant's sole showed as "white toes" at the top of its leg). Every box is therefore given to the
    # game at integer size with the fractional remainder as a CubeDeformation, which grows or shrinks the
    # drawn cube without touching the UV: drawn size exact, net on whole texels.
    nets = []
    for part, e, lf, lt in boxes:
        w, h, dep = [lt[i] - lf[i] for i in range(3)]
        W, H, D = [max(1, round(v)) for v in (w, h, dep)]
        nets.append({"part": part, "e": e, "lf": lf, "lt": lt, "w": W, "h": H, "d": D,
                     "grow": ((w - W) / 2, (h - H) / 2, (dep - D) / 2),
                     "nw": 2 * (W + D), "nh": D + H})
    nets.sort(key=lambda n: -n["nh"])
    # A texture roughly square: the pack width is the power of two nearest the root of the nets' area (with
    # room for the gaps), never narrower than the widest net.
    area = sum((n["nw"] + 1) * (n["nh"] + 1) for n in nets)
    width_units = max(1 << max(4, round(math.log2(math.sqrt(area * 1.3)))), max(n["nw"] for n in nets))
    x = y = row_h = 0
    for n in nets:
        if x + n["nw"] > width_units:
            x, y, row_h = 0, y + row_h, 0
        n["u"], n["v"] = x, y
        x += n["nw"] + 1
        row_h = max(row_h, n["nh"] + 1)
    height_units = y + row_h
    # declared texture size for LayerDefinition (units); the image is scale x that
    tex_units = (1 << (width_units - 1).bit_length(), 1 << max(1, (height_units - 1).bit_length()))
    out = Image.new("RGBA", (tex_units[0] * scale, tex_units[1] * scale), (0, 0, 0, 0))

    def face_image(e, face):
        f = e["faces"].get(face)
        if not f or f.get("texture") is None:
            return None
        x1, y1, x2, y2 = f["uv"]
        flip_x, flip_y = x2 < x1, y2 < y1
        px = [v * tex.width / d["resolution"]["width"] for v in (min(x1, x2), min(y1, y2), max(x1, x2), max(y1, y2))]
        box = tuple(int(round(v)) for v in px)
        if box[2] <= box[0] or box[3] <= box[1]:
            return None
        im = tex.crop(box)
        if flip_x: im = im.transpose(Image.FLIP_LEFT_RIGHT)
        if flip_y: im = im.transpose(Image.FLIP_TOP_BOTTOM)
        r = f.get("rotation", 0)
        if r: im = im.rotate(-r, expand=True)
        if f.get("shade"):   # a face we invented (a cut, a plug): darker, so a seam reads as a crease
            k = f["shade"]
            im = Image.merge("RGBA", [c.point(lambda v: int(v * k)) for c in im.split()[:3]] + [im.split()[3]])
        return im

    def place(im, u, v, w, h):
        if im is None or w <= 0 or h <= 0:
            return
        W, H = max(1, int(round(w * scale))), max(1, int(round(h * scale)))
        out.paste(im.resize((W, H), Image.NEAREST), (int(round(u * scale)), int(round(v * scale))))

    for n in nets:
        e, u, v, w, h, dep = n["e"], n["u"], n["v"], n["w"], n["h"], n["d"]
        up = face_image(e, "up"); down = face_image(e, "down")
        place(up.rotate(180) if up else None, u + dep, v, w, dep)                      # java DOWN cell = world top
        place(down.transpose(Image.FLIP_LEFT_RIGHT) if down else None, u + dep + w, v, w, dep)  # java UP cell = world bottom
        place(face_image(e, "east"), u, v + dep, dep, h)                              # java WEST = model -x = bb +x
        place(face_image(e, "north"), u + dep, v + dep, w, h)
        place(face_image(e, "west"), u + dep + w, v + dep, dep, h)
        place(face_image(e, "south"), u + 2 * dep + w, v + dep, w, h)

    tex_out = os.path.join(ROOT, "src", "main", "resources", "assets", "cityworld_zoo", "textures", "entity", (texture_as or rig_name) + ".png")
    out.save(tex_out)
    if texture_as:
        print(f"{texture_as}: texture only, {out.width}x{out.height} on the {rig_name} packing; wrote {tex_out}")
        return

    # Java mesh body.
    lines = []
    by_part = {}
    for n in nets:
        by_part.setdefault(n["part"], []).append(n)
    var = {}
    for name in order:
        p = parts[name]
        parent_var = "root" if p["parent"] is None else var[p["parent"]]
        pj = to_java(p["pivot"]) if p["parent"] is None else to_java_local(sub(p["pivot"], parts[p["parent"]]["pivot"]))
        rx, ry, rz = p["rotation"]
        cubes = []
        for n in by_part.get(name, []):
            lf, lt = n["lf"], n["lt"]
            gx, gy, gz = n["grow"]
            jmin = [-lt[0] + gx, -lt[1] + gy, lf[2] + gz]   # the integer box sits centred in the real one
            if any(abs(g) > 1e-6 for g in (gx, gy, gz)):
                cubes.append(f'.texOffs({n["u"]}, {n["v"]}).addBox({fmt(jmin[0])}, {fmt(jmin[1])}, {fmt(jmin[2])}, {fmt(n["w"])}, {fmt(n["h"])}, {fmt(n["d"])}, new CubeDeformation({fmt(gx)}, {fmt(gy)}, {fmt(gz)}))')
            else:
                cubes.append(f'.texOffs({n["u"]}, {n["v"]}).addBox({fmt(jmin[0])}, {fmt(jmin[1])}, {fmt(jmin[2])}, {fmt(n["w"])}, {fmt(n["h"])}, {fmt(n["d"])})')
        builder = "CubeListBuilder.create()" + "".join("\n                        " + c for c in cubes)
        if any((rx, ry, rz)):
            pose = f"PartPose.offsetAndRotation({fmt(pj[0])}, {fmt(pj[1])}, {fmt(pj[2])}, {fmt(math.radians(-rx))}, {fmt(math.radians(-ry))}, {fmt(math.radians(rz))})"
        else:
            pose = f"PartPose.offset({fmt(pj[0])}, {fmt(pj[1])}, {fmt(pj[2])})"
        v = name.replace("_", "")
        var[name] = v
        lines.append(f'        PartDefinition {v} = {parent_var}.addOrReplaceChild("{name}",\n                {builder},\n                {pose});')
    mesh_out = os.path.join(ROOT, "blockbench", "generated", rig_name + "_mesh.java.txt")
    os.makedirs(os.path.dirname(mesh_out), exist_ok=True)
    open(mesh_out, "w").write(f"// LayerDefinition.create(mesh, {tex_units[0]}, {tex_units[1]}); texture {out.width}x{out.height}\n" + "\n".join(lines) + "\n")

    # A box-UV .bbmodel of the result (modded_entity format), texture embedded, for editing from here on.
    def bb_group(name):
        p = parts[name]
        rx, ry, rz = p["rotation"]
        return {"name": name, "origin": list(p["pivot"]), "rotation": [rx, ry, rz], "uuid": str(uuid.uuid4()),
                "export": True, "isOpen": False, "visibility": True, "autouv": 0, "children": []}
    bb_groups = {name: bb_group(name) for name in order}
    bb_elements = []
    for n in nets:
        p = parts[n["part"]]
        e = n["e"]
        # world (unrotated-frame) from/to = pivot + local in the artist's frame
        f = add(p["pivot"], n["lf"]); t = add(p["pivot"], n["lt"])
        el = {"name": e["name"], "box_uv": True, "rescale": False, "locked": False, "from": f, "to": t,
              "autouv": 0, "color": 0, "origin": list(p["pivot"]), "uv_offset": [n["u"], n["v"]], "type": "cube",
              "uuid": str(uuid.uuid4())}
        bb_elements.append(el)
        bb_groups[n["part"]]["children"].append(el["uuid"])
    outliner = []
    for name in order:
        g = bb_groups[name]
        parent = parts[name]["parent"]
        (bb_groups[parent]["children"] if parent else outliner).append(g)
    buf = io.BytesIO(); out.save(buf, "PNG")
    bbmodel = {
        "meta": {"format_version": "4.10", "model_format": "modded_entity", "box_uv": True},
        "name": rig_name, "model_identifier": "", "visible_box": [1, 1, 0], "variable_placeholders": "",
        "variable_placeholder_buttons": [], "timeline_setups": [], "unhandled_root_fields": {},
        "resolution": {"width": tex_units[0], "height": tex_units[1]},
        "elements": bb_elements, "outliner": outliner,
        "textures": [{"path": "", "name": rig_name + ".png", "folder": "", "namespace": "", "id": "0", "particle": False,
                      "render_mode": "default", "render_sides": "auto", "frame_time": 1, "frame_order_type": "loop",
                      "frame_order": "", "frame_interpolate": False, "visible": True, "mode": "bitmap", "saved": False,
                      "uuid": str(uuid.uuid4()), "relative_path": "", "source": "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode()}],
    }
    bb_out = os.path.join(ROOT, "blockbench", rig_name + ".bbmodel")
    json.dump(bbmodel, open(bb_out, "w"), separators=(",", ":"))
    print(f"{len(nets)} boxes in {len(order)} parts; texture {out.width}x{out.height} (declared {tex_units[0]}x{tex_units[1]})")
    print("wrote", tex_out); print("wrote", mesh_out); print("wrote", bb_out)

if __name__ == "__main__":
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    texture_as = next((a.split("=", 1)[1] for a in sys.argv if a.startswith("--texture-as=")), None)
    convert(args[0], args[1], args[2] if len(args) > 2 else None, texture_as)
