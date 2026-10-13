#!/usr/bin/env python3
"""A model of ours, from a spec, to three outputs: the Java mesh, a placeholder texture, and a Blockbench file.

For animals Sable has not drawn yet, the source of truth is a SPEC below rather than a .bbmodel: a tree of
parts (pivot, rotation, parent) each holding boxes (position, size, colour) in Java model units (y down, 24 at
the ground, x positive = the animal's left). The script packs one box net per box into a texture at
PX_PER_UNIT, paints each with flat colours (the same painter tools/textures/paint.py uses), writes the Java
`mesh()` body, and writes blockbench/<name>.bbmodel (modded-entity format, box UV, texture embedded) in
Blockbench's coordinates (y up, 0 at the ground, x mirrored) for painting or reshaping. A reshaped model
comes back through bbmodel_to_java.py like any of Sable's.

    python3 tools/blockbench/spec_to_model.py deer
"""
import base64, io, json, math, os, random, sys, uuid

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "textures"))
import paint  # noqa: E402  (the painter: box nets, face shading, patterns)
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(__file__), "..", "..")

def P(name, parent, pivot, rot=(0, 0, 0), boxes=()):
    return {"name": name, "parent": parent, "pivot": pivot, "rot": rot, "boxes": list(boxes)}

def B(label, x, y, z, w, h, d, colour):
    return {"label": label, "from": (x, y, z), "size": (w, h, d), "colour": colour}

BROWN = (140, 96, 58); PALE = (230, 220, 200); ANTLER = (110, 90, 70); HOOF = (60, 45, 35)

SPECS = {
    "deer": {
        "px_per_unit": 2,
        "scale": 0.8,            # applied in Java (MeshTransformer.scaling), not in the spec
        "pattern": paint.speckle((200, 170, 130), 0.06),
        "parts": [
            P("body", None, (0, 8, 1), boxes=[B("Body", -3, -3, -8, 6, 7, 16, BROWN)]),
            P("neck", None, (0, 6, -6), rot=(0.5, 0, 0), boxes=[B("Neck", -1.5, -8, -2.5, 3, 8, 4, BROWN)]),
            P("head", "neck", (0, -8, 0), rot=(-0.5, 0, 0), boxes=[
                B("Head", -2, -2, -6, 4, 4, 6, BROWN),
                B("Left ear", 2, -4, -2, 2, 3, 1, BROWN), B("Right ear", -4, -4, -2, 2, 3, 1, BROWN),
                B("Left antler", 1, -8, -2, 1, 5, 1, ANTLER), B("Right antler", -2, -8, -2, 1, 5, 1, ANTLER),
                B("Left tine", 2, -7, -2, 1, 3, 1, ANTLER), B("Right tine", -3, -7, -2, 1, 3, 1, ANTLER)]),
            # Legs: a short upper from the hip and a longer lower from the knee (a deer's length is in the
            # cannon), 5 + 7 of the 12 units; the lower ends in a darker hoof band painted, not modelled.
            P("left_front_leg", None, (2, 12, -5), boxes=[B("Left front upper", -1, 0, -1, 2, 5, 2, BROWN)]),
            P("left_front_lower", "left_front_leg", (0, 5, 0), boxes=[B("Left front lower", -1, 0, -1, 2, 7, 2, BROWN)]),
            P("right_front_leg", None, (-2, 12, -5), boxes=[B("Right front upper", -1, 0, -1, 2, 5, 2, BROWN)]),
            P("right_front_lower", "right_front_leg", (0, 5, 0), boxes=[B("Right front lower", -1, 0, -1, 2, 7, 2, BROWN)]),
            P("left_hind_leg", None, (2, 12, 6), boxes=[B("Left hind upper", -1, 0, -1, 2, 5, 2, BROWN)]),
            P("left_hind_lower", "left_hind_leg", (0, 5, 0), boxes=[B("Left hind lower", -1, 0, -1, 2, 7, 2, BROWN)]),
            P("right_hind_leg", None, (-2, 12, 6), boxes=[B("Right hind upper", -1, 0, -1, 2, 5, 2, BROWN)]),
            P("right_hind_lower", "right_hind_leg", (0, 5, 0), boxes=[B("Right hind lower", -1, 0, -1, 2, 7, 2, BROWN)]),
            P("tail", None, (0, 6, 9), rot=(0.4, 0, 0), boxes=[B("Tail", -1, -2, -1, 2, 4, 1, PALE)]),
        ],
    },
}

def fmt(f):
    s = f"{f:.3f}".rstrip("0").rstrip(".")
    return (s if "." in s else s + ".0") + "F"

def build(name):
    spec = SPECS[name]
    ppu = spec["px_per_unit"]
    parts = {p["name"]: p for p in spec["parts"]}
    # absolute java pivots (rest pose, rotations ignored, as Blockbench stores positions)
    def absolute(pname):
        p = parts[pname]
        return p["pivot"] if p["parent"] is None else tuple(a + b for a, b in zip(absolute(p["parent"]), p["pivot"]))

    # pack nets: width 64 units, rows
    nets = []
    for p in spec["parts"]:
        for b in p["boxes"]:
            w, h, d = b["size"]
            nets.append({"part": p["name"], "box": b, "nw": math.ceil(2 * (w + d)), "nh": math.ceil(d + h)})
    nets.sort(key=lambda n: -n["nh"])
    width_units = 64
    x = y = row_h = 0
    for n in nets:
        if x + n["nw"] > width_units:
            x, y, row_h = 0, y + row_h, 0
        n["u"], n["v"] = x, y
        x += n["nw"] + 1
        row_h = max(row_h, n["nh"] + 1)
    height_units = 1 << max(1, (y + row_h - 1).bit_length())
    tex_units = (width_units, height_units)

    # the placeholder texture, box nets painted at ppu
    im = Image.new("RGBA", (tex_units[0] * ppu, tex_units[1] * ppu), (0, 0, 0, 0))
    draw = ImageDraw.Draw(im)
    rng = random.Random(name)
    for n in nets:
        w, h, d = n["box"]["size"]
        paint.paint_box(draw, (n["u"] * ppu, n["v"] * ppu, w * ppu, h * ppu, d * ppu, n["box"]["label"]), n["box"]["colour"], spec["pattern"], rng)
    tex_out = os.path.join(ROOT, "src", "main", "resources", "assets", "cityworld_zoo", "textures", "entity", name + ".png")
    im.save(tex_out)

    # Java mesh
    lines = []
    var = {}
    for p in spec["parts"]:
        parent_var = "root" if p["parent"] is None else var[p["parent"]]
        cubes = "".join(f'\n                        .texOffs({n["u"]}, {n["v"]}).addBox({fmt(n["box"]["from"][0])}, {fmt(n["box"]["from"][1])}, {fmt(n["box"]["from"][2])}, {fmt(n["box"]["size"][0])}, {fmt(n["box"]["size"][1])}, {fmt(n["box"]["size"][2])})'
                        for n in nets if n["part"] == p["name"])
        px, py, pz = p["pivot"]
        rx, ry, rz = p["rot"]
        pose = (f"PartPose.offsetAndRotation({fmt(px)}, {fmt(py)}, {fmt(pz)}, {fmt(rx)}, {fmt(ry)}, {fmt(rz)})" if any(p["rot"])
                else f"PartPose.offset({fmt(px)}, {fmt(py)}, {fmt(pz)})")
        v = p["name"].replace("_", "")
        var[p["name"]] = v
        lines.append(f'        PartDefinition {v} = {parent_var}.addOrReplaceChild("{p["name"]}",\n                CubeListBuilder.create(){cubes},\n                {pose});')
    mesh_out = os.path.join(ROOT, "blockbench", "generated", name + "_mesh.java.txt")
    os.makedirs(os.path.dirname(mesh_out), exist_ok=True)
    open(mesh_out, "w").write(f"// LayerDefinition.create(mesh, {tex_units[0]}, {tex_units[1]}); texture {im.width}x{im.height}\n" + "\n".join(lines) + "\n")

    # Blockbench: java (x, y, z) -> (-x, 24 - y, z); a box from/to mirrors in x. Rotations (-rx, -ry, rz), degrees.
    groups = {}
    elements = []
    for p in spec["parts"]:
        ax, ay, az = absolute(p["name"])
        rx, ry, rz = p["rot"]
        g = {"name": p["name"], "origin": [-ax, 24 - ay, az],
             "rotation": [-math.degrees(rx), -math.degrees(ry), math.degrees(rz)],
             "uuid": str(uuid.uuid4()), "export": True, "isOpen": True, "visibility": True, "autouv": 0, "children": []}
        groups[p["name"]] = g
        for n in [n for n in nets if n["part"] == p["name"]]:
            b = n["box"]
            fx, fy, fz = b["from"]; w, h, d = b["size"]
            jx0, jy0, jz0 = ax + fx, ay + fy, az + fz
            el = {"name": b["label"], "box_uv": True, "rescale": False, "locked": False,
                  "from": [-(jx0 + w), 24 - (jy0 + h), jz0], "to": [-jx0, 24 - jy0, jz0 + d],
                  "autouv": 0, "color": 0, "origin": [-ax, 24 - ay, az], "uv_offset": [n["u"], n["v"]],
                  "type": "cube", "uuid": str(uuid.uuid4())}
            elements.append(el)
            g["children"].append(el["uuid"])
    outliner = []
    for p in spec["parts"]:
        (groups[p["parent"]]["children"] if p["parent"] else outliner).append(groups[p["name"]])
    buf = io.BytesIO(); im.save(buf, "PNG")
    bb = {"meta": {"format_version": "4.10", "model_format": "modded_entity", "box_uv": True},
          "name": name, "model_identifier": "", "visible_box": [1, 1, 0], "variable_placeholders": "",
          "variable_placeholder_buttons": [], "timeline_setups": [], "unhandled_root_fields": {},
          "resolution": {"width": tex_units[0], "height": tex_units[1]},
          "elements": elements, "outliner": outliner,
          "textures": [{"path": "", "name": name + ".png", "folder": "", "namespace": "", "id": "0", "particle": False,
                        "render_mode": "default", "render_sides": "auto", "frame_time": 1, "frame_order_type": "loop",
                        "frame_order": "", "frame_interpolate": False, "visible": True, "mode": "bitmap", "saved": False,
                        "uuid": str(uuid.uuid4()), "relative_path": "", "source": "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode()}]}
    bb_out = os.path.join(ROOT, "blockbench", name + ".bbmodel")
    json.dump(bb, open(bb_out, "w"), separators=(",", ":"))
    print(f"{name}: {len(nets)} boxes in {len(spec['parts'])} parts; texture {im.width}x{im.height} (declared {tex_units[0]}x{tex_units[1]})")
    for f in (tex_out, mesh_out, bb_out): print("wrote", f)

if __name__ == "__main__":
    build(sys.argv[1])
