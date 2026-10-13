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

    python3 tools/blockbench/bbmodel_to_java.py giraffe <model.bbmodel> <texture.png>
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

# --- the work ----------------------------------------------------------------------------------------------
def convert(rig_name, bb_path, tex_path):
    rig = RIGS[rig_name]
    scale = rig["texture_scale"]
    d = json.load(open(bb_path))
    tex = Image.open(tex_path).convert("RGBA")
    elements = {e["uuid"]: e for e in d["elements"]}
    groups = []  # top-level groups as lists of element dicts, in order
    for node in d["outliner"]:
        if isinstance(node, dict):
            groups.append([elements[c] for c in node.get("children", []) if isinstance(c, str)])
        else:
            groups.append([elements[node]])

    # Assign elements to parts.
    parts = {}
    order = []
    for name, parent, pivot, members in rig["parts"]:
        found = []
        for m in members:
            gi, ename = m[0], m[1]
            nth = m[2] if len(m) > 2 else None
            matches = [e for e in groups[gi] if e["name"] == ename]
            if nth is not None:
                matches = [matches[nth]]
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
                    sub_name = name + "_" + e["name"].lower().replace(" ", "_")
                    parts[sub_name] = {"parent": name, "pivot": pivot, "elements": [e], "rotation": tuple(rot)}
                    order.append(sub_name)
                    boxes.append((sub_name, e, lf, lt))
            else:
                boxes.append((name, e, sub(e["from"], p["pivot"]), sub(e["to"], p["pivot"])))

    # Pack a box net per element: cell sizes in model units, placed at integer unit offsets.
    nets = []
    for part, e, lf, lt in boxes:
        w, h, dep = [lt[i] - lf[i] for i in range(3)]
        nets.append({"part": part, "e": e, "lf": lf, "lt": lt, "w": w, "h": h, "d": dep,
                     "nw": math.ceil(2 * (w + dep)), "nh": math.ceil(dep + h)})
    nets.sort(key=lambda n: -n["nh"])
    width_units = 256
    x = y = row_h = 0
    for n in nets:
        if x + n["nw"] > width_units:
            x, y, row_h = 0, y + row_h, 0
        n["u"], n["v"] = x, y
        x += n["nw"] + 1
        row_h = max(row_h, n["nh"] + 1)
    height_units = y + row_h
    # declared texture size for LayerDefinition (units); the image is scale x that
    tex_units = (width_units, 1 << max(1, (height_units - 1).bit_length()))
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

    tex_out = os.path.join(ROOT, "src", "main", "resources", "assets", "cityworld_zoo", "textures", "entity", rig_name + ".png")
    out.save(tex_out)

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
            jmin = [-lt[0], -lt[1], lf[2]]
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
    convert(sys.argv[1], sys.argv[2], sys.argv[3])
